package smartdesk.booking.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import smartdesk.booking.service.NoShowService;

@Component
public class NoShowScheduler {

    private final NoShowService noShowService;

    public NoShowScheduler(NoShowService noShowService) {
        this.noShowService = noShowService;
    }

    @Scheduled(fixedRate = 60_000)
    public void processNoShows() {
        noShowService.releaseNoShows();
    }
}