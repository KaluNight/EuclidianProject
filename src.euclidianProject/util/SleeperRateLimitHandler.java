package util;

import net.rithms.riot.api.request.ratelimit.DefaultRateLimitHandler;
import net.rithms.riot.api.request.ratelimit.RespectedRateLimitException;
import net.rithms.riot.api.request.Request;

public class SleeperRateLimitHandler extends DefaultRateLimitHandler {

  @Override
  public void onRequestAboutToFire(Request request) throws RespectedRateLimitException {

    while(true) {
      if(!isRateLimitExceeded(request)) {
        break;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    super.onRequestAboutToFire(request);
  }
}
