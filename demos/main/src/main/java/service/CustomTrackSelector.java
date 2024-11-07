package service;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CustomTrackSelector extends DefaultTrackSelector {
  public CustomTrackSelector(Context context) {
    super(context);
  }

  @Nullable
  @Override
  public  <T extends TrackInfo<T>> Pair<ExoTrackSelection.Definition, Integer> selectTracksForType(
      @C.TrackType int trackType,
      MappedTrackInfo mappedTrackInfo,
      @RendererCapabilities.Capabilities int[][][] formatSupport,
      TrackInfo.Factory<T> trackInfoFactory,
      Comparator<List<T>> selectionComparator) {

    ArrayList<List<T>> possibleSelections = new ArrayList<>();
    int rendererCount = mappedTrackInfo.getRendererCount();

    for (int rendererIndex = 0; rendererIndex < rendererCount; rendererIndex++) {
      if (trackType == mappedTrackInfo.getRendererType(rendererIndex)) {
        TrackGroupArray groups = mappedTrackInfo.getTrackGroups(rendererIndex);
        for (int groupIndex = 0; groupIndex < groups.length; groupIndex++) {
          TrackGroup trackGroup = groups.get(groupIndex);
          @RendererCapabilities.Capabilities int[] groupSupport = formatSupport[rendererIndex][groupIndex];
          List<T> trackInfos = trackInfoFactory.create(rendererIndex, trackGroup, groupSupport);

          for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
            T trackInfo = trackInfos.get(trackIndex);
            boolean isSelected = false;

            // Determine if track is eligible for selection
            if (trackInfo.getSelectionEligibility() != SELECTION_ELIGIBILITY_NO) {
              isSelected = true;
              Log.d("CustomTrackSelector", "Track Selected: Index=" + trackIndex
                  + ", Bitrate=" + trackInfo.format.bitrate
                  + ", RendererIndex=" + rendererIndex
                  + ", Selected=" + isSelected);
            } else {
              Log.d("CustomTrackSelector", "Track Skipped: Index=" + trackIndex
                  + ", Bitrate=" + trackInfo.format.bitrate
                  + ", RendererIndex=" + rendererIndex
                  + ", Selected=" + isSelected);
            }

            // Add the track to the possible selections if eligible
            if (isSelected) {
              List<T> selection = Collections.singletonList(trackInfo);
              possibleSelections.add(selection);
            }
          }
        }
      }
    }

    // Get the best track selection using the comparator, and log it
    if (possibleSelections.isEmpty()) {
      return null;
    }

    List<T> bestSelection = Collections.max(possibleSelections, selectionComparator);
    Log.d("CustomTrackSelector", "Best Track Selection: " + bestSelection.get(0).format.bitrate);

    int[] trackIndices = new int[bestSelection.size()];
    for (int i = 0; i < bestSelection.size(); i++) {
      trackIndices[i] = bestSelection.get(i).trackIndex;
    }

    T firstTrackInfo = bestSelection.get(0);
    return Pair.create(
        new ExoTrackSelection.Definition(firstTrackInfo.trackGroup, trackIndices),
        firstTrackInfo.rendererIndex);
  }
}
