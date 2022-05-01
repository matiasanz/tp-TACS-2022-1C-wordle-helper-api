package Tournaments;

import com.tacs2022.wordlehelper.domain.Language;
import com.tacs2022.wordlehelper.domain.tournaments.Tournament;
import com.tacs2022.wordlehelper.domain.tournaments.TournamentStatus;
import com.tacs2022.wordlehelper.domain.tournaments.Visibility;
import com.tacs2022.wordlehelper.domain.user.User;
import com.tacs2022.wordlehelper.dtos.tournaments.NewTournamentDto;
import com.tacs2022.wordlehelper.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.of;
import static org.junit.jupiter.api.Assertions.*;

class TournamentsTest {
	Tournament tournament;
	LocalDate startDate = of(2016, 6, 10);
	LocalDate endDate = of(2016, 6, 12);

	@BeforeEach
	public void fixture() throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecurityService ss = new SecurityService();
		byte[] salt = ss.getSalt();

		User julian = new User("Julian", ss.hash("1234", salt), salt);
		NewTournamentDto tournamentDto = new NewTournamentDto("Luchemos por la vida", startDate, endDate,
				Visibility.PUBLIC, List.of(Language.EN, Language.ES));
		tournament = new Tournament(tournamentDto, julian);
	}

	@Test
	void validateInmutability() {
		assertNotEquals(endDate, endDate.plusDays(1));
	}

	@Test
	void tournamentFinished() {
		assertEquals(TournamentStatus.FINISHED, tournament.getStatus());
	}

	@Test
	void tournamentPublic() {
		assertFalse(tournament.isPrivate());
	}
	
    @Test
    void daysPassedUntilDate(){
        assertEquals(0, tournament.getDaysPlayedAtDate(startDate));
        assertEquals(1, tournament.getDaysPlayedAtDate(startDate.plusDays(1)));
        assertEquals(2, tournament.getDaysPlayedAtDate(endDate));
        assertEquals(3, tournament.getDaysPlayedAtDate(endDate.plusDays(1)));
    }
}
