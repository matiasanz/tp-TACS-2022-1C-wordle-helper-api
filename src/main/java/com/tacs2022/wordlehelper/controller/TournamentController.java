package com.tacs2022.wordlehelper.controller;

import com.tacs2022.wordlehelper.domain.tournaments.Leaderboard;
import com.tacs2022.wordlehelper.domain.tournaments.Tournament;
import com.tacs2022.wordlehelper.service.TournamentService;
import com.tacs2022.wordlehelper.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

@RequestMapping("/tournaments")
@RestController()
public class TournamentController {
    @Autowired
    TournamentService tournamentService;
    @Autowired
    UserService userService;

    @GetMapping()
    public Map<String, List<Tournament>> getAllTournaments() {
        List<Tournament> allTournaments = tournamentService.findAll();
        Map<String, List<Tournament>> response = new HashMap<>();
        response.put("tournaments", allTournaments);
        return response;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public Tournament create(@RequestBody Tournament tournament){
        return tournamentService.save(tournament);
    }

    @GetMapping("/{id}")
    public Tournament getTournamentById(@PathVariable(value = "id") Long id) {
        return tournamentService.findById(id);
    }

    @GetMapping("/{id}/leaderboard")
    public Leaderboard getLeaderboardByTournamentId(@PathVariable(value = "id") Long tournamentId){
        return tournamentService.getTournamentLeaderboard(tournamentId);
    }

	@PostMapping("/{id}/participants")
    public ResponseEntity<Map<String, String>> addParticipant(@RequestBody Map<String, Long> body, @PathVariable(value = "id") Long tournamentId){
        Tournament tournament = tournamentService.findById(tournamentId);
        Long idParticipant = body.get("idParticipant");

        if(idParticipant == null){ //TODO: Manejar con excepcion
            Map<String, String> missingAttributes = new HashMap<>();
            missingAttributes.put("missingAttributes", "idParticipant");
            return ResponseEntity.badRequest().body(missingAttributes);
        }

        tournament.addParticipant(userService.findById(idParticipant));
        return ResponseEntity.noContent().build();
    }

}
