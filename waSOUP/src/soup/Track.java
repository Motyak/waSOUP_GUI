package soup;

public class Track
{
	public int _id;
	public String title;
	public String artist;
	public int _duration;
	public String _md5;
	
	public Track()
	{
		this._id = -1;
		this.title = "";
		this.artist = "";
		this._duration = -1;
		this._md5 = "";
	}
	
	public Track(String title)
	{
		this._id = -1;
		this.title = title;
		this.artist = "";
		this._duration = -1;
		this._md5 = "";
	}

	public Track(int id, String title, String artist, int duration, String md5)
	{
		this._id = id;
		this.title = title;
		this.artist = artist;
		this._duration = duration;
		this._md5 = md5;
	}
}
