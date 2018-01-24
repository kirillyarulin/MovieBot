public class Movie {
    private int id;
    private String title;
    private int year;
    private String overview;
    private String posterPath;
    private float voteAverage;

    public Movie(int id, String title, int year, String overview, String posterPath, float voteAverage) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.overview = overview;
        this.posterPath = posterPath;
        this.voteAverage = voteAverage;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public float getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(float voteAverage) {
        this.voteAverage = voteAverage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
