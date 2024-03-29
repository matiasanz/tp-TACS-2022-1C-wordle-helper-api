package Tournaments;

import Utils.TournamentFactory;
import Utils.UserFactory;
import com.tacs2022.wordlehelper.domain.Language;
import com.tacs2022.wordlehelper.domain.tournaments.Scoreboard;
import com.tacs2022.wordlehelper.domain.tournaments.Tournament;
import com.tacs2022.wordlehelper.domain.user.Result;
import com.tacs2022.wordlehelper.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static java.time.LocalDate.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LeaderboardTest {

    Tournament tournament;
    User user1;
    int failedAttempts;
    LocalDate startDate = of(2016, 6, 10);
    LocalDate endDate = of(2016, 6, 12);

    @BeforeEach
    public void fixture(){
        user1 = UserFactory.userWithName("felipe");
            user1.addResult(new Result(2, Language.ES, startDate));
            user1.addResult(new Result(3, Language.ES, startDate.plusDays(1)));

        failedAttempts = 5;

        tournament = TournamentFactory.tournamentBetweenDates(startDate, endDate);
            tournament.setLanguages(List.of(Language.ES));
            tournament.addParticipant(user1);
    }

    @Test
    public void userPlaysAllDaysAndHisScoreEqualsToHisFailAttempts() {
        assertEquals(failedAttempts, new Scoreboard(user1, tournament).getScoreAtDate(endDate));
    }

    @Test
    public void scoreIgnoresResultsOfExcludedLanguages(){
        user1.addResult(new Result(3, Language.EN, startDate));
        assertEquals(5, new Scoreboard(user1, tournament).getScoreAtDate(endDate));
    }

    @Test
    public void scoreIgnoresResultsOutOfTournamentsPeriod(){
        user1.addResult(new Result(1, Language.ES, startDate.minusDays(1)));
        user1.addResult(new Result(0, Language.ES, endDate.plusDays(1)));
        assertEquals(5, new Scoreboard(user1, tournament).getScoreAtDate(endDate));
    }

    @Test
    public void daysNotPlayedAreCorrectlyPenalized(){
        LocalDate endDateModified = endDate.plusDays(2);
        tournament.setEndDate(endDateModified);

        assertEquals(19, new Scoreboard(user1, tournament).getScoreAtDate(endDateModified));
    }

    @Test
    void daysPassedUntilDateInDatesBeforeTournament(){
        assertEquals(0, tournament.getDaysPlayedUntilDate(startDate.minusDays(2)));
        assertEquals(0, tournament.getDaysPlayedUntilDate(startDate.minusDays(1)));
    }

    @Test
    void daysPassedUntilDateAtStartDate(){
        assertEquals(0, tournament.getDaysPlayedUntilDate(startDate));
    }

    @Test
    void daysPassedUntilDateInMiddleDays(){
        assertEquals(1, tournament.getDaysPlayedUntilDate(startDate.plusDays(1)));
        assertEquals(2, tournament.getDaysPlayedUntilDate(startDate.plusDays(2)));
    }

    @Test
    void daysPassedSinceTournamentEnds(){
        assertEquals(2, tournament.getDaysPlayedUntilDate(endDate));
        assertEquals(3, tournament.getDaysPlayedUntilDate(endDate.plusDays(1)));
        assertEquals(3, tournament.getDaysPlayedUntilDate(endDate.plusDays(2)));
    }

    @Test
    public void penalizationByDatesForSingleLanguageTournament(){
        User carlitos = UserFactory.userWithName("Carlitos");
        Function<LocalDate, Integer> carlitosScoreByDate = fecha -> new Scoreboard(carlitos, tournament).getScoreAtDate(fecha);

        assertEquals(0, carlitosScoreByDate.apply(startDate.minusDays(1)));
        assertEquals(0, carlitosScoreByDate.apply(startDate));
        assertEquals(7, carlitosScoreByDate.apply(startDate.plusDays(1)));
        assertEquals(14, carlitosScoreByDate.apply(endDate));
        assertEquals(21, carlitosScoreByDate.apply(endDate.plusDays(1)));
        assertEquals(21, carlitosScoreByDate.apply(endDate.plusDays(2)));
    }

    @Test
    public void penalizationByDatesForMultipleLanguagesTournament(){
        tournament.setLanguages(List.of(Language.ES, Language.EN));
        User carlitos = UserFactory.userWithName("Carlitos");
        Function<LocalDate, Integer> carlitosScoreByDate = fecha -> new Scoreboard(carlitos, tournament).getScoreAtDate(fecha);
        assertEquals(0, carlitosScoreByDate.apply(startDate.minusDays(1)));
        assertEquals(0, carlitosScoreByDate.apply(startDate));
        assertEquals(14, carlitosScoreByDate.apply(startDate.plusDays(1)));
        assertEquals(28, carlitosScoreByDate.apply(endDate));
        assertEquals(42, carlitosScoreByDate.apply(endDate.plusDays(1)));
        assertEquals(42, carlitosScoreByDate.apply(endDate.plusDays(2)));
    }

    @Test
    public void userPlaysEveryDayAndIsNotPenalized(){
        user1.addResult(new Result(1, Language.ES, endDate));
        assertEquals(failedAttempts+1, new Scoreboard(user1, tournament).getScoreAtDate(endDate.plusDays(2)));
    }

    @Test
    public void userPlaysOneLanguageOfManyAndGetsHalfPenalized(){
        tournament.setLanguages(List.of(Language.ES, Language.EN));
        assertEquals(failedAttempts+14, new Scoreboard(user1, tournament).getScoreAtDate(startDate.plusDays(2)));
    }


}
