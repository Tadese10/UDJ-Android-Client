#include "UDJServerConnection.hpp"
#include <QDesktopServices>
#include <QDir>
#include <QSqlQuery>

namespace UDJ{


UDJServerConnection::UDJServerConnection(){

}

UDJServerConnection::~UDJServerConnection(){
	musicdb.close();
}

void UDJServerConnection::startConnection(){
  QDir dbDir(QDesktopServices::storageLocation(QDesktopServices::DataLocation));  
  if(!dbDir.exists()){
    //TODO handle if this fails
    dbDir.mkpath(dbDir.absolutePath());
  }
  musicdb = QSqlDatabase::addDatabase("QSQLITE", getMusicDBConnectionName());
  musicdb.setDatabaseName(dbDir.absoluteFilePath(getMusicDBName()));
  musicdb.open(); 

  QSqlQuery setupQuery(musicdb);
	bool worked = true;
  worked = setupQuery.exec("CREATE TABLE IF NOT EXISTS library "
    "(id INTEGER PRIMARY KEY AUTOINCREMENT, "
   "song TEXT NOT NULL, artist TEXT, album TEXT, filePath TEXT)");  
	PRINT_SQLERROR("Error creating library table", setupQuery)	

  worked = setupQuery.exec("CREATE TABLE IF NOT EXISTS mainplaylist "
   "(id INTEGER PRIMARY KEY AUTOINCREMENT, "
   "libraryId INTEGER REFERENCES library (id) ON DELETE CASCADE, "
   "voteCount INTEGER DEFAULT 1, "
   "timeAdded INTEGER DEFAULT CURRENT_TIMESTAMP);");
	PRINT_SQLERROR("Error creating mainplaylist table.", setupQuery)	

  worked = setupQuery.exec("CREATE VIEW IF NOT EXISTS main_playlist_view "
    "AS SELECT "
    "mainplaylist.id AS plId, "
    "mainplaylist.libraryId AS libraryId, "
    "library.song AS song, "
    "library.artist AS artist, "
    "library.album AS album, "
    "library.filePath AS filePath, "
    "mainplaylist.voteCount AS voteCount, "
    "mainplaylist.timeAdded AS timeAdded "
    "FROM mainplaylist INNER JOIN library ON "
    "mainplaylist.libraryId = library.id ORDER BY mainplaylist.voteCount DESC, mainplaylist.timeAdded;");
	PRINT_SQLERROR("Error creating main_playlist_view view.", setupQuery)	

  worked = setupQuery.exec("CREATE TRIGGER IF NOT EXISTS updateVotes INSTEAD OF "
    "UPDATE ON main_playlist_view BEGIN "
    "UPDATE mainplaylist SET voteCount=new.voteCount "
    "WHERE  mainplaylist.id = old.plId;"
    "END;");
	PRINT_SQLERROR("Error creating update trigger for main_playlist_view.", setupQuery)	

	worked = setupQuery.exec("CREATE TRIGGER IF NOT EXISTS deleteSongFromPlaylist "
	"INSTEAD OF DELETE ON main_playlist_view "
	"BEGIN "
	"DELETE FROM mainplaylist "
	"where mainplaylist.id = old.plId; "
	"END;");
	PRINT_SQLERROR("Error creating delete trigger for main_playlist_view.", setupQuery)	

  worked = setupQuery.exec("CREATE TRIGGER IF NOT EXISTS insertOnPlaylist INSTEAD OF "
    "INSERT ON main_playlist_view BEGIN "
    "INSERT INTO mainplaylist "
    "(libraryId) VALUES (new.libraryId);"
    "END;");
	PRINT_SQLERROR("Error creating insert trigger for main_playlist_view.", setupQuery)	

}


}//end namespace
