package smartdesk.booking.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import smartdesk.booking.dto.cache.TeamCacheData;
import smartdesk.booking.entity.Team;
import smartdesk.booking.exception.ResourceNotFoundException;
import smartdesk.booking.repository.TeamRepository;

@Service
public class TeamCacheService {

    private final TeamRepository teamRepository;

    public TeamCacheService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Cacheable(cacheNames = "teamMetadata", key = "#teamId")
    public TeamCacheData getTeamMetadata(Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Team not found: " + teamId
                        )
                );

        return new TeamCacheData(
                team.getId(),
                team.getName()
        );
    }
}