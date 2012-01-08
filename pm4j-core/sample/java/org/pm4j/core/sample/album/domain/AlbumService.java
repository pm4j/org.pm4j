package org.pm4j.core.sample.album.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A service to provide music album domain objects for the demo UI.
 */
public class AlbumService {
  
  private Map<Long, Album> idToAlbumMap = new TreeMap<Long, Album>();
  private long nextId = 1;
 
  public AlbumService() {
    addAlbumToList("HQ", "Roy Harper", false, null);
    addAlbumToList("The Rough Dancer and Cyclical Night", "Astor Piazzola", false, null);
    addAlbumToList("The Black Light", "Calexico", false, null);
    addAlbumToList("Symphony No.5", "CBSO", true, "Sibelius");
  }

  /**
   * Provides the set of known albums.<br>
   * The set is decoupled from the internal data set to simulate somehow an
   * external data storage.
   * 
   * @return The current set of albums.
   */
  public Collection<Album> getAlbumList() {
    List<Album> listCopy = new ArrayList<Album>(idToAlbumMap.values());
    
    for (int i=0; i<listCopy.size(); ++i)
      listCopy.set(i, listCopy.get(i).clone());
    
    return listCopy;
  }
  
  /**
   * Saves a new or updated album instance.
   * @param album
   */
  public void save(Album album) {
    if (album.getId() == null) {
      album.setId(nextId++);
    }
    
    idToAlbumMap.put(album.getId(), album);
  }


  private void addAlbumToList(String title, String artist, boolean classical, String composer) {
    Album album = new Album();
    album.setTitle(title);
    album.setArtist(artist);
    album.setClassical(classical);
    album.setComposer(composer);
    save(album);
  }
  
}
