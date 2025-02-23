package sh.okx.rankup.requirements;

import static org.junit.jupiter.api.Assertions.*;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;
import sh.okx.rankup.RankupTest;
import sh.okx.rankup.ranks.Rank;

public class MobKillsRequirementsTest extends RankupTest {

  public MobKillsRequirementsTest() {
    super("mobkillsrequirements");
  }

  @Test
  public void testMobKillsRequirements() {
    PlayerMock player = server.addPlayer();

    player.setStatistic(Statistic.KILL_ENTITY, EntityType.SNOWMAN, 2);
    player.setStatistic(Statistic.KILL_ENTITY, EntityType.MUSHROOM_COW, 1);

    Rank rank = plugin.getRankups().findTrackOrDefault(player).getFirst().getRank();

    assertEquals(3 - 2, rank.getRequirement(player, "mob-kills#snow_golem").getRemaining(player));
    assertEquals(3 - 1, rank.getRequirement(player, "mob-kills#mushroom_cow").getRemaining(player));
  }
}
