package sh.okx.rankup.ranks;

import java.util.*;
import java.util.function.Predicate;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sh.okx.rankup.RankupPlugin;

public abstract class RankList<T extends Rank> {

  protected RankupPlugin plugin;
  @Getter
  private final Set<RankTrack<T>> trackSet = new HashSet<>();

  public RankList(RankupPlugin plugin, Collection<? extends T> ranks) {
    this.plugin = plugin;
    List<RankElement<T>> rankElements = new ArrayList<>();
    for (T rank : ranks) {
      if (getRankByName(rank.getRank()).isPresent()) {
        plugin.getLogger().warning("Duplicate rank names are not supported: " + rank.getRank());
        continue;
      }

      if (rank != null && validateSection(rank)) {
        // find next
        rankElements.add(findNext(rank, rankElements));
      } else {
        plugin.getLogger().warning("Ignoring rank: " + rank);
      }
    }

    for (RankElement<T> rankElement : rankElements) {
      if (rankElement.isRootNode()) {
        RankTrack<T> rootedTree = new RankTrack<>(rankElement);
        trackSet.add(rootedTree);
        addLastRank(plugin, rootedTree);
      }
    }
  }

  public Set<T> getAll() {
    Set<T> ranks = new HashSet<>();
    for (RankTrack<T> track : trackSet) {
      ranks.addAll(track.asRankList());
    }
    return ranks;
  }

  protected abstract void addLastRank(RankupPlugin plugin, RankTrack<T> tree);

  private RankElement<T> findNext(T rank, List<RankElement<T>> rankElements) {
    Objects.requireNonNull(rank);

    RankElement<T> currentElement = new RankElement<>(rank, null);

    for (RankElement<T> rankElement : rankElements) {
      T rank1 = rankElement.getRank();
      if (rank1.getRank() != null
          && rank1.getRank().equalsIgnoreCase(rank.getNext())) {
        // current rank element is the next rank
        currentElement.setNext(rankElement);
      } else if (rank1.getNext() != null
          && rank1.getNext().equalsIgnoreCase(rank.getRank())) {
        rankElement.setNext(currentElement);
      }
    }
    return currentElement;
  }

  protected boolean validateSection(T rank) {
    String name = rank.getRank() == null ? "rank" : rank.getRank();
    String nextField = rank.getNext();
    if (nextField == null || nextField.isEmpty()) {
      plugin.getLogger().warning("Rankup section " + name + " does not have a 'next' field.");
      plugin.getLogger().warning("Having a final rank (for example: \"Z: rank: 'Z'\") from 3.4.2 or earlier should no longer be used.");
      plugin.getLogger().warning("If this is intended as a final rank, you should delete " + name);
      return false;
    } else if (rank.getRequirements() == null) {
      plugin.getLogger().warning("Rank " + name + " does not have any requirements.");
      return false;
    }
    return true;
  }

  public Optional<T> findRank(@NotNull Predicate<T> filter) {
    for (RankTrack<T> track : trackSet) {
      for (T rank : track) {
        if (filter.test(rank)) {
          return Optional.of(rank);
        }
      }
    }

    return Optional.empty();
  }

  public Optional<RankElement<T>> findRankElement(@NotNull Predicate<RankElement<T>> filter) {
    for (RankTrack<T> track : trackSet) {
      for (RankElement<T> element : track.asElementList()) {
        if (filter.test(element)) {
          return Optional.of(element);
        }
      }
    }

    return Optional.empty();
  }

  /**
   * Find the {@link RankTrack} which a {@link Player} is currently on. If they are on multiple tracks, the first will
   * be returned.
   *
   * @param player the player to find the track of
   * @return optionally the first track the player is on
   */
  public Optional<RankTrack<T>> findTrack(@NotNull Player player) {
    for (RankTrack<T> track : trackSet) {
      if (track.getFirst().getRank().isIn(player)) {
        return Optional.of(track);
      }
    }
    return Optional.empty();
  }

  /**
   * Finds the {@link RankTrack} which a {@link Player} is on or returns one of the available tracks at random.
   *
   * @param player the player to try to find the track of
   * @return the relevant track or a random track if {@link #findTrack(Player)} is empty
   */
  public RankTrack<T> findTrackOrDefault(@NotNull Player player) {
    return findTrack(player).orElse(trackSet.iterator().next());
  }

  /**
   * Finds the {@link RankTrack} which contains the given rank.
   *
   * @param rank the rank to compare with tracks
   * @return the track which contains the rank, if one exists
   */
  public Optional<RankTrack<T>> findTrack(@NotNull T rank) {
    return trackSet.stream().filter(track -> track.contains(rank)).findAny();
  }

  public Optional<T> getRankByName(@Nullable String name) {
    if (name == null) {
      return Optional.empty();
    } else {
      return findRank(rank -> rank.getRank().equalsIgnoreCase(name));
    }
  }

  public Optional<RankElement<T>> getByName(String name) {
    if (name == null) {
      return Optional.empty();
    } else {
      return findRankElement(element -> name.equals(element.getRank().getRank()));
    }
  }


  public RankElement<T> getByPlayer(@NotNull Player player) {
    return findRankElement(element -> element.getRank().isIn(player)).orElse(null);
  }

  public T getRankByPlayer(Player player) {
    return findRank(rank -> rank.isIn(player)).orElse(null);
  }
}
