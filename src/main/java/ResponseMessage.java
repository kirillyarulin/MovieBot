import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.Discover;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.MovieDb;


import java.util.*;

public class ResponseMessage {
    private final Random random = new Random();
    private final TmdbApi tmdbApi;
    private Map<String, Integer> genreMap;
    private MovieDb movieDb;

    private final String language = "ru";
    private final int currentYear;
    private final String prefImage = "https://image.tmdb.org/t/p/w300";

    //filters
    private Integer firstYear;
    private Integer lastYear;
    private float voteAverage = 3;
    private TreeSet<Integer> genres;

    private StateOfResponseMessage state = StateOfResponseMessage.EMPTY;
    private HashSet<Integer> pastMovies;

    public StateOfResponseMessage getState() {
        return state;
    }

    public ResponseMessage(String apiKey) {
        this.tmdbApi = new TmdbApi(apiKey);
        this.genres = new TreeSet<>();
        this.pastMovies = new HashSet<>();
        genreMap = createGenreMap();
        currentYear = Calendar.getInstance().get(Calendar.YEAR);
    }

    public Movie getRandomMovie() throws RuntimeException {
        if (state == StateOfResponseMessage.READY) {
            movieDb = new MovieDb();
            int page;
            int i = 16;
            while (movieDb.getTitle() == null && i-- > 0) {
                try {
                    if (i > 12) {
                        page = random.nextInt(30)+1;
                    } else if (i > 7) {
                        page = random.nextInt(10)+1;
                    } else if (i > 3) {
                        page = random.nextInt(3)+1;
                    } else {
                        page = 1;
                    }
                    movieDb = tmdbApi.getDiscover().getDiscover(new Discover().includeAdult(false)
                            .releaseDateGte(firstYear + "-01-01")
                            .year(lastYear)
                            .page(page)
                            .withGenres(getStringOfGenres())
                            .voteAverageGte(voteAverage)
                            .language(language))
                            .getResults()
                            .get(random.nextInt(20));

                    if (pastMovies.contains(movieDb.getId())) {
                        movieDb = null;
                    }
                } catch (Exception e) {
                    //empty
                }
            }

            if (movieDb == null) {
                throw new RuntimeException("too few movies");
            }

            pastMovies.add(movieDb.getId());
            return new Movie(
                    movieDb.getId(),
                    movieDb.getTitle(),
                    Integer.parseInt(movieDb.getReleaseDate().substring(0, 4)),
                    movieDb.getOverview(),
                    prefImage + movieDb.getPosterPath(),
                    movieDb.getVoteAverage());
        } else {
            throw new RuntimeException("There are not enough filters to get a movie");
        }
    }

    public Set<String> getAvailableGenres() {
        return genreMap.keySet();
    }

    public void addGenres(String genres) throws RuntimeException {
        this.genres = new TreeSet<>();
        Set<String> set = new TreeSet<>(Arrays.asList(genres.split(" *, *")));
        set.retainAll(genreMap.keySet());

        if (!set.isEmpty()) {
            state = StateOfResponseMessage.READY;
            for (String x : set) {
                this.genres.add(genreMap.get(x));
            }
        } else {
            throw new RuntimeException("Incorrect genres");
        }
    }

    public void setRangeOfYears(int firstYear, int lastYear) throws RuntimeException {
        if (lastYear > currentYear) { lastYear = currentYear; }
        if (firstYear >= 0 && lastYear >= 0 && firstYear <= lastYear && lastYear >= 1896) {
            this.firstYear = firstYear;
            this.lastYear = lastYear;
            if (state != StateOfResponseMessage.READY) { state = StateOfResponseMessage.WITH_RANGE_OF_YEARS; }

        } else {
            throw new RuntimeException("Incorrect range of years");
        }
    }

    public void setVoteAverage(float voteAverage) throws RuntimeException {
        if (voteAverage > 0 && voteAverage < 10) {
            this.voteAverage = voteAverage;
        } else {
            throw new RuntimeException("Incorrect vote average");
        }
    }


    private Map<String, Integer> createGenreMap() {
        List<Genre> genreList = tmdbApi.getGenre().getGenreList(language);
        Map<String, Integer> map = new HashMap<>();
        for (Genre x : genreList) {
            map.put(x.getName(), x.getId());
        }
        return map;
    }

    private String getStringOfGenres() {
        StringBuilder str = new StringBuilder();
        for (int x : genres) {
            str.append(x).append(",");
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }

    enum StateOfResponseMessage {EMPTY, WITH_RANGE_OF_YEARS, READY}
}


