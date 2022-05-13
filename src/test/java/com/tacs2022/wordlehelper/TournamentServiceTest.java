package com.tacs2022.wordlehelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.tacs2022.wordlehelper.domain.Language;
import com.tacs2022.wordlehelper.domain.tournaments.Scoreboard;
import com.tacs2022.wordlehelper.domain.tournaments.Tournament;
import com.tacs2022.wordlehelper.domain.tournaments.TournamentStatus;
import com.tacs2022.wordlehelper.domain.tournaments.Visibility;
import com.tacs2022.wordlehelper.domain.user.Result;
import com.tacs2022.wordlehelper.domain.user.User;
import com.tacs2022.wordlehelper.exceptions.ForbiddenException;
import com.tacs2022.wordlehelper.repos.TournamentRepository;
import com.tacs2022.wordlehelper.service.SecurityService;
import com.tacs2022.wordlehelper.service.TournamentService;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TournamentServiceTest {

	@MockBean
	TournamentRepository tournamentRepoMock;
	
	@Mock
	Tournament tournamentMock;
	
	@Autowired
	TournamentService tournamentService;
	
	Tournament privateTournament;
	Tournament publicTournament;
	
	LocalDate startDate;
	LocalDate endDate;
	
	User julian;
	User agus;
	
	@BeforeEach
	public void fixture() throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecurityService ss = new SecurityService();
		byte[] salt = ss.getSalt();

		julian = new User("Julian", ss.hash("1234", salt), salt);
		agus = new User("Agus", ss.hash("password", salt), salt);
		
		startDate = LocalDate.now().plusWeeks(1);
		endDate = LocalDate.now().plusWeeks(2);
		
		julian.addResult(new Result(2, Language.ES, startDate));
		julian.addResult(new Result(3, Language.ES, startDate.plusDays(1)));
		julian.addResult(new Result(4, Language.ES, startDate.plusDays(2)));
		
		agus.addResult(new Result(1, Language.ES, startDate));
		agus.addResult(new Result(7, Language.ES, startDate.plusDays(1)));
		agus.addResult(new Result(4, Language.ES, startDate.plusDays(2)));
		
		privateTournament = new Tournament("Superliga", startDate, endDate,
				Visibility.PRIVATE, List.of(Language.EN, Language.ES), julian);
		publicTournament = new Tournament("Ligue 1", startDate, endDate,
				Visibility.PUBLIC, List.of(Language.EN, Language.ES), julian);
			
	}
	
	@Test
	public void youCannotAddParticipantsToTournamentsThatHaveAStartedStatus() {
		Mockito.when(tournamentRepoMock.findById(anyLong())).thenReturn(Optional.of(tournamentMock));
		Mockito.when(tournamentMock.getStatus()).thenReturn(TournamentStatus.STARTED);
		Assertions
			.assertThatThrownBy ( () -> { tournamentService.addParticipant(Long.valueOf(1), julian, agus); } )
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("Participants cannot be added to this tournament once it has started or finished");
	}
	
	@Test
	public void youCannotAddParticipantsToTournamentsThatHaveAFinishedStatus() {
		Mockito.when(tournamentRepoMock.findById(anyLong())).thenReturn(Optional.of(tournamentMock));
		Mockito.when(tournamentMock.getStatus()).thenReturn(TournamentStatus.FINISHED);
		Assertions
			.assertThatThrownBy ( () -> { tournamentService.addParticipant(Long.valueOf(1), julian, agus); } )
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("Participants cannot be added to this tournament once it has started or finished");
	}
	
	@Test
	public void userTriedToAddParticipantToPrivateTournamentWithoutBeingTheOwner() {
		Mockito.when(tournamentRepoMock.findById(anyLong())).thenReturn(Optional.of(tournamentMock));
		Mockito.when(tournamentMock.getStatus()).thenReturn(TournamentStatus.NOTSTARTED);
		Mockito.when(tournamentMock.getVisibility()).thenReturn(Visibility.PRIVATE);
		Mockito.when(tournamentMock.userIsOwner(any(User.class))).thenReturn(false);
		Assertions
			.assertThatThrownBy ( () -> { tournamentService.addParticipant(Long.valueOf(1), julian, agus); } )
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("User cannot add participant to this private tournament without being the owner");
	}
	
	@Test
	public void userTriedToAddParticipantToPublicTournamentWithoutBeingTheOwnerCanOnlyAddSelf() {
		Mockito.when(tournamentRepoMock.findById(anyLong())).thenReturn(Optional.of(tournamentMock));
		Mockito.when(tournamentMock.getStatus()).thenReturn(TournamentStatus.NOTSTARTED);
		Mockito.when(tournamentMock.getVisibility()).thenReturn(Visibility.PUBLIC);
		Mockito.when(tournamentMock.userIsOwner(any(User.class))).thenReturn(false);
		Assertions
			.assertThatThrownBy ( () -> { tournamentService.addParticipant(Long.valueOf(1), agus, julian); } )
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("User can only add another participant to public tournament if owner");
	}
	
	@Test
	public void asAUserIWantToBeAbleToAddAnotherUserToAPrivateTournamentThatICreated() {
		Mockito.when(tournamentRepoMock.findById(anyLong())).thenReturn(Optional.of(privateTournament));
		Assertions.assertThatNoException()
			.isThrownBy(() -> { tournamentService.addParticipant(Long.valueOf(1), julian, agus); });
	}
	
	@Test 
	public void asAUserIWantToBeAbleToAddAnotherUserToAPublicTournamentThatICreated() {
		Mockito.when(tournamentRepoMock.findById(anyLong())).thenReturn(Optional.of(publicTournament));
		Assertions.assertThatNoException()
			.isThrownBy(() -> { tournamentService.addParticipant(Long.valueOf(1), julian, agus); });
	}
	
	@Test
	public void asAUserIWantToBeAbleToJoinAPublicTournamentThatHasNotStartedYet(){
		Mockito.when(tournamentRepoMock.findById(anyLong())).thenReturn(Optional.of(publicTournament));
		Assertions.assertThatNoException()
			.isThrownBy(() -> { tournamentService.addParticipant(Long.valueOf(1), agus, agus); });
	}
	
	@Test
	public void theParticipantWithTheFewestAttemptsWins() {
		publicTournament.addParticipant(agus);
		Mockito.when(tournamentRepoMock.findById(anyLong())).thenReturn(Optional.of(publicTournament));
		List<Scoreboard> leaderboard = tournamentService.getTournamentLeaderboard(Long.valueOf(1) , startDate.plusDays(2), julian);
		Scoreboard scoreboardOne = leaderboard.get(0);
		Scoreboard scoreboardTwo = leaderboard.get(1);
		assertTrue(scoreboardOne.getTotalAttempts() < scoreboardTwo.getTotalAttempts());
		assertEquals(julian.getUsername(), scoreboardOne.getUser().getUsername());
	}
	
	@Test
	public void publicTournamentsTheyAreVisibleToAll() {
		Mockito.when(tournamentRepoMock.findById(anyLong())).thenReturn(Optional.of(publicTournament));
		Assertions.assertThatNoException()
		.isThrownBy(() -> { tournamentService.getByIdAndValidateVisibility(Long.valueOf(1), agus); });
	}
	
	@Test
	public void privateTournamentsTheyAreVisibleOnlyByThePersonWhoCreatedThem() {
		Mockito.when(tournamentRepoMock.findById(anyLong())).thenReturn(Optional.of(privateTournament));
		Assertions
			.assertThatThrownBy ( () -> { tournamentService.getByIdAndValidateVisibility(Long.valueOf(1), agus); } )
			.isInstanceOf(ForbiddenException.class)
			.hasMessage("User does not have permissions to view this tournament");
	}
	
	@Test
	public void privateTournamentsTheyAreVisibleByThePersonWhoCreatedThem() {
		Mockito.when(tournamentRepoMock.findById(anyLong())).thenReturn(Optional.of(privateTournament));
		Assertions.assertThatNoException()
			.isThrownBy(() -> { tournamentService.getByIdAndValidateVisibility(Long.valueOf(1), julian); });
	}
	
	@Test
	public void privateTournamentsTheyAreVisibleByThoseWhoHaveJoined(){
		privateTournament.addParticipant(agus);
		Mockito.when(tournamentRepoMock.findById(anyLong())).thenReturn(Optional.of(privateTournament));
		Assertions.assertThatNoException()
			.isThrownBy(() -> { tournamentService.getByIdAndValidateVisibility(Long.valueOf(1), agus); });
	}
}
