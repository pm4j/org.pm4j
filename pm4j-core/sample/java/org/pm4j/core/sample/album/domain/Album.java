package org.pm4j.core.sample.album.domain;

/**
 * A music album domain object.
 * <p>
 * Inspired by Martin Fowler. See: http://martinfowler.com/eaaDev/PmObject.html
 */
public class Album implements Cloneable {

  /** Primary key for identification in storage. */
  private Long id;
  
  private String title;

  private String artist;
  
  private Boolean classical;
  
  private String composer;
  
  @Override
  public Album clone() {
    try {
      return (Album) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
  
  // -- Getter/Setter --
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getArtist() {
    return artist;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public Boolean isClassical() {
    return classical;
  }

  public void setClassical(Boolean classical) {
    this.classical = classical;
  }

  public String getComposer() {
    return composer;
  }

  public void setComposer(String composer) {
    this.composer = composer;
  }

}
