/**
 * Copyright 2011 Kurtis L. Nusbaum
 * 
 * This file is part of UDJ.
 * 
 * UDJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * UDJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with UDJ.  If not, see <http://www.gnu.org/licenses/>.
 */
#ifndef ACTIVE_PLAYLIST_VIEW_HPP
#define ACTIVE_PLAYLIST_VIEW_HPP
#include "ConfigDefs.hpp"
#include <QTableView>
#include <vector>

class QSqlRelationalTableModel;
class QAction;

namespace UDJ{

class DataStore;

/**
 * \brief Used to dislay the active playlist.
 */
class ActivePlaylistView : public QTableView{
Q_OBJECT
public:

  /** @name Constructors */
  //@{

  /**
   * \brief Constructs an ActivePlaylistView
   *
   * @param dataStore The DataStore backing this instance of UDJ.
   * @param parent The parent widget.
   */
  ActivePlaylistView(DataStore* dataStore, QWidget* parent=0);

  //@}

private:

  /** @name Private Functions */
  //@{
 
  /**
   * \brief Retrieves the playlist id's of the currently selected songs.
   *
   * @return The playlist id's of the currently selected songs.
   */
  std::vector<playlist_song_id_t> getSelectedSongs() const;

  /**
   * \brief Initializes actions used in the ActivePlaylistView
   */
  void createActions();

  /** @name Private Members */
  //@{

  /**
   * \brief The data store containing music that could potentially be added
   * to the playlist.
   */
  DataStore* dataStore;

  /**
   * \brief The model backing the view
   */
  QSqlRelationalTableModel *model;

  /**
   * \brief Action used to remove songs from the active playlist.
   */
  QAction *removeSongAction;

  //@}

   /** @name Private Slots */
   //@{
private slots:

  /**
   * \brief Refreshes the data to be displayed.
   */
  void refreshDisplay(); 

  /**
   * \brief Takes the given index, identifies the song it corresponds to,
   * and sets that as the current song being played.
   *
   * @param index The model index of the playlist entry that should be set
   *  as the currenty song.
   */
  void setCurrentSong(const QModelIndex& index);

  /**
   * \breif .
   */
  void handleContextMenuRequest(const QPoint& pos);

  /**
   * \brief Removes all the currently selected songs from the active playlist.
   */
  void removeSongs();
  
  //@}

};


} //end namespace
#endif //ACTIVE_PLAYLIST_VIEW_HPP
