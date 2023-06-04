package sh.okx.rankup.ranks;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RankTrack<T extends Rank> implements Iterable<T> {
  private final RankElement<T> first;

  public RankTrack(RankElement<T> first) {
    this.first = first;
  }

  public RankElement<T> getFirst() {
    return first;
  }

  public int length() {
    int len = 0;
    RankElement<T> elem = first;
    while (elem != null) {
      len++;
      elem = elem.getNext();
    }
    return len;
  }

  public boolean contains(T element) {
    for (T containedElement : this) {
      if (containedElement.equals(element) || containedElement.getRank().equals(element.getRank())) {
        return true;
      }
    }

    return false;
  }

  @NotNull
  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      private RankElement<T> element = first;
      @Override
      public boolean hasNext() {
        return element != null;
      }

      @Override
      public T next() {
        T rank = element.getRank();
        element = element.getNext();
        return rank;
      }
    };
  }

  public List<RankElement<T>> asElementList() {
    List<RankElement<T>> ranks = new ArrayList<>();
    RankElement<T> elem = first;
    while (elem != null) {
      ranks.add(elem);
      elem = elem.getNext();
    }
    return ranks;
  }

  public List<T> asRankList() {
    List<T> ranks = new ArrayList<>();
    for (T rank : this) {
      ranks.add(rank);
    }
    return ranks;
  }

  public RankElement<T> last() {
    RankElement<T> elem = first;
    while (elem.hasNext()) {
      elem = elem.getNext();
    }
    return elem;
  }
}
