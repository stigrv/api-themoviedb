/*
 *      Copyright (c) 2004-2012 YAMJ Members
 *      http://code.google.com/p/moviejukebox/people/list
 *
 *      Web: http://code.google.com/p/moviejukebox/
 *
 *      This software is licensed under a Creative Commons License
 *      See this page: http://code.google.com/p/moviejukebox/wiki/License
 *
 *      For any reuse or distribution, you must make clear to others the
 *      license terms of this work.
 */
package com.moviejukebox.themoviedb;

import com.moviejukebox.themoviedb.MovieDbException.MovieDbExceptionType;
import com.moviejukebox.themoviedb.model.*;
import com.moviejukebox.themoviedb.tools.ApiUrl;
import com.moviejukebox.themoviedb.tools.FilteringLayout;
import com.moviejukebox.themoviedb.tools.WebBrowser;
import com.moviejukebox.themoviedb.wrapper.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * The MovieDb API
 *
 * This is for version 3 of the API as specified here:
 * http://help.themoviedb.org/kb/api/about-3
 *
 * @author stuart.boston
 */
public class TheMovieDb {

    private static final Logger LOGGER = Logger.getLogger(TheMovieDb.class);
    private String apiKey;
    private TmdbConfiguration tmdbConfig;
    /*
     * API Methods
     *
     * These are not set to static so that multiple instances of
     * the API can co-exist
     *
     * TODO: See issue 9 http://code.google.com/p/themoviedbapi/issues/detail?id=9
     */
    private static final String BASE_MOVIE = "movie/";
    private static final String BASE_PERSON = "person/";
    private static final String BASE_COMPANY = "company/";
    private static final String BASE_GENRE = "genre/";
    private static final String BASE_AUTH = "authentication/";
    private static final String BASE_COLLECTION = "collection/";
    private static final String BASE_ACCOUNT = "account/";
    // Configuration
    private final ApiUrl tmdbConfigUrl = new ApiUrl(this, "configuration");
    // Authentication
    private final ApiUrl tmdbAuthToken = new ApiUrl(this, BASE_AUTH, "token/new");
    private final ApiUrl tmdbAuthSession = new ApiUrl(this, BASE_AUTH, "session/new");
    // Account
    private final ApiUrl tmdbAccount = new ApiUrl(this, BASE_ACCOUNT);
    private final ApiUrl tmdbFavouriteMovies = new ApiUrl(this, BASE_ACCOUNT, "/favorite_movies");
    private final ApiUrl tmdbPostFavourite = new ApiUrl(this, BASE_ACCOUNT, "/favorite");
    private final ApiUrl tmdbRatedMovies = new ApiUrl(this, BASE_ACCOUNT, "/rated_movies");
    private final ApiUrl tmdbMovieWatchList = new ApiUrl(this, BASE_ACCOUNT, "/movie_watchlist");
    private final ApiUrl tmdbPostMovieWatchList = new ApiUrl(this, BASE_ACCOUNT, "/movie_watchlist");
    // Movies
    private final ApiUrl tmdbMovieInfo = new ApiUrl(this, BASE_MOVIE);
    private final ApiUrl tmdbMovieAltTitles = new ApiUrl(this, BASE_MOVIE, "/alternative_titles");
    private final ApiUrl tmdbMovieCasts = new ApiUrl(this, BASE_MOVIE, "/casts");
    private final ApiUrl tmdbMovieImages = new ApiUrl(this, BASE_MOVIE, "/images");
    private final ApiUrl tmdbMovieKeywords = new ApiUrl(this, BASE_MOVIE, "/keywords");
    private final ApiUrl tmdbMovieReleaseInfo = new ApiUrl(this, BASE_MOVIE, "/releases");
    private final ApiUrl tmdbMovieTrailers = new ApiUrl(this, BASE_MOVIE, "/trailers");
    private final ApiUrl tmdbMovieTranslations = new ApiUrl(this, BASE_MOVIE, "/translations");
    private final ApiUrl tmdbMovieSimilarMovies = new ApiUrl(this, BASE_MOVIE, "/similar_movies");
    private final ApiUrl tmdbLatestMovie = new ApiUrl(this, BASE_MOVIE, "/latest");
    private final ApiUrl tmdbUpcoming = new ApiUrl(this, BASE_MOVIE, "/upcoming");
    private final ApiUrl tmdbNowPlaying = new ApiUrl(this, BASE_MOVIE, "/now-playing");
    private final ApiUrl tmdbPopularMovieList = new ApiUrl(this, BASE_MOVIE, "/popular");
    private final ApiUrl tmdbTopRatedMovies = new ApiUrl(this, BASE_MOVIE, "/top-rated");
    private final ApiUrl tmdbPostRating = new ApiUrl(this, BASE_MOVIE, "/rating");
    // Collections
    private final ApiUrl tmdbCollectionInfo = new ApiUrl(this, BASE_COLLECTION);
    private final ApiUrl tmdbCollectionImages = new ApiUrl(this, BASE_COLLECTION, "/images");
    // People
    private final ApiUrl tmdbPersonInfo = new ApiUrl(this, BASE_PERSON);
    private final ApiUrl tmdbPersonCredits = new ApiUrl(this, BASE_PERSON, "/credits");
    private final ApiUrl tmdbPersonImages = new ApiUrl(this, BASE_PERSON, "/images");
    // Companies
    private final ApiUrl tmdbCompanyInfo = new ApiUrl(this, BASE_COMPANY);
    private final ApiUrl tmdbCompanyMovies = new ApiUrl(this, BASE_COMPANY, "/movies");
    // Genres
    private final ApiUrl tmdbGenreList = new ApiUrl(this, BASE_GENRE, "/list");
    private final ApiUrl tmdbGenreMovies = new ApiUrl(this, BASE_GENRE, "/movies");
    // Search
    private final ApiUrl tmdbSearchMovie = new ApiUrl(this, "search/movie");
    private final ApiUrl tmdbSearchPeople = new ApiUrl(this, "search/person");
    private final ApiUrl tmdbSearchCompanies = new ApiUrl(this, "search/company");

    /*
     * Jackson JSON configuration
     */
    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * API for The Movie Db.
     *
     * @param apiKey
     * @throws MovieDbException
     */
    public TheMovieDb(String apiKey) throws MovieDbException {
        this.apiKey = apiKey;
        URL configUrl = tmdbConfigUrl.getQueryUrl("");
        String webpage = WebBrowser.request(configUrl);
        FilteringLayout.addApiKey(apiKey);

        try {
            WrapperConfig wc = mapper.readValue(webpage, WrapperConfig.class);
            tmdbConfig = wc.getTmdbConfiguration();
        } catch (IOException ex) {
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, "Failed to read configuration", ex);
        }
    }

    /**
     * Output the API version information to the debug log
     */
    public static void showVersion() {
        String apiTitle = TheMovieDb.class.getPackage().getSpecificationTitle();

        if (StringUtils.isNotBlank(apiTitle)) {
            String apiVersion = TheMovieDb.class.getPackage().getSpecificationVersion();
            String apiRevision = TheMovieDb.class.getPackage().getImplementationVersion();
            StringBuilder sv = new StringBuilder();
            sv.append(apiTitle).append(" ");
            sv.append(apiVersion).append(" r");
            sv.append(apiRevision);
            LOGGER.debug(sv.toString());
        } else {
            LOGGER.debug("API-TheMovieDb version/revision information not available");
        }
    }

    /**
     * Get the API key that is to be used
     *
     * @return
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Set the proxy information
     *
     * @param host
     * @param port
     * @param username
     * @param password
     */
    public void setProxy(String host, String port, String username, String password) {
        WebBrowser.setProxyHost(host);
        WebBrowser.setProxyPort(port);
        WebBrowser.setProxyUsername(username);
        WebBrowser.setProxyPassword(password);
    }

    /**
     * Set the connection and read time out values
     *
     * @param connect
     * @param read
     */
    public void setTimeout(int connect, int read) {
        WebBrowser.setWebTimeoutConnect(connect);
        WebBrowser.setWebTimeoutRead(read);
    }

    /**
     * Compare the MovieDB object with a title & year
     *
     * @param moviedb The moviedb object to compare too
     * @param title The title of the movie to compare
     * @param year The year of the movie to compare
     * @return True if there is a match, False otherwise.
     */
    public static boolean compareMovies(MovieDb moviedb, String title, String year) {
        if ((moviedb == null) || (StringUtils.isBlank(title))) {
            return false;
        }

        if (StringUtils.isNotBlank(year) && !year.equalsIgnoreCase("UNKNOWN") && StringUtils.isNotBlank(moviedb.getReleaseDate())) {
            // Compare with year
            String movieYear = moviedb.getReleaseDate().substring(0, 4);
            if (movieYear.equals(year)) {
                if (moviedb.getOriginalTitle().equalsIgnoreCase(title)) {
                    return true;
                }

                if (moviedb.getTitle().equalsIgnoreCase(title)) {
                    return true;
                }
            }
        }

        // Compare without year
        if (moviedb.getOriginalTitle().equalsIgnoreCase(title)) {
            return true;
        }

        if (moviedb.getTitle().equalsIgnoreCase(title)) {
            return true;
        }

        return false;
    }

    /**
     * Search Movies This is a good starting point to start finding movies on
     * TMDb.
     *
     * The idea is to be a quick and light method so you can iterate through
     * movies quickly.
     *
     * http://help.themoviedb.org/kb/api/search-movies
     *
     * TODO: Make the allResults work
     *
     * @param movieName
     * @param language
     * @param allResults
     * @return
     * @throws MovieDbException
     */
    public List<MovieDb> searchMovie(String movieName, String language, boolean allResults) throws MovieDbException {

        URL url = tmdbSearchMovie.getQueryUrl(movieName, language, 1);
        String webpage = WebBrowser.request(url);
        try {
            WrapperMovie wrapper = mapper.readValue(webpage, WrapperMovie.class);
            return wrapper.getMovies();
        } catch (IOException ex) {
            LOGGER.warn("Failed to find movie: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve all of the basic movie information.
     *
     * It will return the single highest rated poster and backdrop.
     *
     * @param movieId
     * @param language
     * @return
     * @throws MovieDbException
     */
    public MovieDb getMovieInfo(int movieId, String language) throws MovieDbException {

        URL url = tmdbMovieInfo.getIdUrl(movieId, language);
        String webpage = WebBrowser.request(url);
        try {
            return mapper.readValue(webpage, MovieDb.class);
        } catch (IOException ex) {
            LOGGER.warn("Failed to get movie info: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve all of the basic movie information.
     *
     * It will return the single highest rated poster and backdrop.
     *
     * @param imdbId
     * @param language
     * @return
     * @throws MovieDbException
     */
    public MovieDb getMovieInfoImdb(String imdbId, String language) throws MovieDbException {

        URL url = tmdbMovieInfo.getIdUrl(imdbId, language);
        String webpage = WebBrowser.request(url);
        try {
            return mapper.readValue(webpage, MovieDb.class);
        } catch (IOException ex) {
            LOGGER.warn("Failed to get movie info: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve all of the alternative titles we have for
     * a particular movie.
     *
     * @param movieId
     * @param country
     * @return
     * @throws MovieDbException
     */
    public List<AlternativeTitle> getMovieAlternativeTitles(int movieId, String country) throws MovieDbException {

        URL url = tmdbMovieAltTitles.getIdUrl(movieId, ApiUrl.DEFAULT_STRING, country);
        String webpage = WebBrowser.request(url);
        try {
            WrapperAlternativeTitles wrapper = mapper.readValue(webpage, WrapperAlternativeTitles.class);
            return wrapper.getTitles();
        } catch (IOException ex) {
            LOGGER.warn("Failed to get movie alternative titles: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve all of the movie cast information.
     *
     * TODO: Add a function to enrich the data with the people methods
     *
     * @param movieId
     * @return
     * @throws MovieDbException
     */
    public List<Person> getMovieCasts(int movieId) throws MovieDbException {

        List<Person> people = new ArrayList<Person>();

        URL url = tmdbMovieCasts.getIdUrl(movieId);
        String webpage = WebBrowser.request(url);
        try {
            WrapperMovieCasts wrapper = mapper.readValue(webpage, WrapperMovieCasts.class);

            // Add a cast member
            for (PersonCast cast : wrapper.getCast()) {
                Person person = new Person();
                person.addCast(cast.getId(), cast.getName(), cast.getProfilePath(), cast.getCharacter(), cast.getOrder());
                people.add(person);
            }

            // Add a crew member
            for (PersonCrew crew : wrapper.getCrew()) {
                Person person = new Person();
                person.addCrew(crew.getId(), crew.getName(), crew.getProfilePath(), crew.getDepartment(), crew.getJob());
                people.add(person);
            }

            return people;
        } catch (IOException ex) {
            LOGGER.warn("Failed to get movie casts: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method should be used when you’re wanting to retrieve all of the
     * images for a particular movie.
     *
     * @param movieId
     * @param language
     * @return
     * @throws MovieDbException
     */
    public List<Artwork> getMovieImages(int movieId, String language) throws MovieDbException {

        List<Artwork> artwork = new ArrayList<Artwork>();
        URL url = tmdbMovieImages.getIdUrl(movieId, language);
        String webpage = WebBrowser.request(url);
        try {
            WrapperImages wrapper = mapper.readValue(webpage, WrapperImages.class);

            // Add all the posters to the list
            for (Artwork poster : wrapper.getPosters()) {
                poster.setArtworkType(ArtworkType.POSTER);
                artwork.add(poster);
            }

            // Add all the backdrops to the list
            for (Artwork backdrop : wrapper.getBackdrops()) {
                backdrop.setArtworkType(ArtworkType.BACKDROP);
                artwork.add(backdrop);
            }

            return artwork;
        } catch (IOException ex) {
            LOGGER.warn("Failed to get movie images: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve all of the keywords that have been added
     * to a particular movie.
     *
     * Currently, only English keywords exist.
     *
     * @param movieId
     * @return
     * @throws MovieDbException
     */
    public List<Keyword> getMovieKeywords(int movieId) throws MovieDbException {

        URL url = tmdbMovieKeywords.getIdUrl(movieId);
        String webpage = WebBrowser.request(url);

        try {
            WrapperMovieKeywords wrapper = mapper.readValue(webpage, WrapperMovieKeywords.class);
            return wrapper.getKeywords();
        } catch (IOException ex) {
            LOGGER.warn("Failed to get movie keywords: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve all of the release and certification data
     * we have for a specific movie.
     *
     * @param movieId
     * @param language
     * @return
     * @throws MovieDbException
     */
    public List<ReleaseInfo> getMovieReleaseInfo(int movieId, String language) throws MovieDbException {

        URL url = tmdbMovieReleaseInfo.getIdUrl(movieId);
        String webpage = WebBrowser.request(url);

        try {
            WrapperReleaseInfo wrapper = mapper.readValue(webpage, WrapperReleaseInfo.class);
            return wrapper.getCountries();
        } catch (IOException ex) {
            LOGGER.warn("Failed to get movie release information: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve all of the trailers for a particular
     * movie.
     *
     * Supported sites are YouTube and QuickTime.
     *
     * @param movieId
     * @param language
     * @return
     * @throws MovieDbException
     */
    public List<Trailer> getMovieTrailers(int movieId, String language) throws MovieDbException {

        List<Trailer> trailers = new ArrayList<Trailer>();
        URL url = tmdbMovieTrailers.getIdUrl(movieId, language);
        String webpage = WebBrowser.request(url);

        try {
            WrapperTrailers wrapper = mapper.readValue(webpage, WrapperTrailers.class);

            // Add the trailer to the return list along with it's source
            for (Trailer trailer : wrapper.getQuicktime()) {
                trailer.setWebsite(Trailer.WEBSITE_QUICKTIME);
                trailers.add(trailer);
            }
            // Add the trailer to the return list along with it's source
            for (Trailer trailer : wrapper.getYoutube()) {
                trailer.setWebsite(Trailer.WEBSITE_YOUTUBE);
                trailers.add(trailer);
            }
            return trailers;
        } catch (IOException ex) {
            LOGGER.warn("Failed to get movie trailers: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve a list of the available translations for
     * a specific movie.
     *
     * @param movieId
     * @return
     * @throws MovieDbException
     */
    public List<Translation> getMovieTranslations(int movieId) throws MovieDbException {

        URL url = tmdbMovieTranslations.getIdUrl(movieId);
        String webpage = WebBrowser.request(url);

        try {
            WrapperTranslations wrapper = mapper.readValue(webpage, WrapperTranslations.class);
            return wrapper.getTranslations();
        } catch (IOException ex) {
            LOGGER.warn("Failed to get movie tranlations: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve all of the basic information about a
     * movie collection.
     *
     * You can get the ID needed for this method by making a getMovieInfo
     * request for the belongs_to_collection.
     *
     * @param movieId
     * @param language
     * @return
     * @throws MovieDbException
     */
    public CollectionInfo getCollectionInfo(int movieId, String language) throws MovieDbException {

        URL url = tmdbCollectionInfo.getIdUrl(movieId);
        String webpage = WebBrowser.request(url);

        try {
            return mapper.readValue(webpage, CollectionInfo.class);
        } catch (IOException ex) {
            LOGGER.warn("Failed to get movie tranlations: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * Get the configuration information
     *
     * @return
     */
    public TmdbConfiguration getConfiguration() {
        return tmdbConfig;
    }

    /**
     * Generate the full image URL from the size and image path
     *
     * @param imagePath
     * @param requiredSize
     * @return
     * @throws MovieDbException
     */
    public URL createImageUrl(String imagePath, String requiredSize) throws MovieDbException {
        if (!tmdbConfig.isValidSize(requiredSize)) {
            throw new MovieDbException(MovieDbExceptionType.INVALID_IMAGE, requiredSize);
        }

        StringBuilder sb = new StringBuilder(tmdbConfig.getBaseUrl());
        sb.append(requiredSize);
        sb.append(imagePath);
        try {
            return (new URL(sb.toString()));
        } catch (MalformedURLException ex) {
            LOGGER.warn("Failed to create image URL: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.INVALID_URL, sb.toString(), ex);
        }
    }

    /**
     * This is a good starting point to start finding people on TMDb.
     *
     * The idea is to be a quick and light method so you can iterate through
     * people quickly.
     *
     * TODO: Fix allResults
     *
     * @param personName
     * @param allResults
     * @return
     * @throws MovieDbException
     */
    public List<Person> searchPeople(String personName, boolean allResults) throws MovieDbException {

        URL url = tmdbSearchPeople.getQueryUrl(personName, "", 1);
        String webpage = WebBrowser.request(url);

        try {
            WrapperPerson wrapper = mapper.readValue(webpage, WrapperPerson.class);
            return wrapper.getResults();
        } catch (IOException ex) {
            LOGGER.warn("Failed to find person: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve all of the basic person information.
     *
     * It will return the single highest rated profile image.
     *
     * @param personId
     * @return
     * @throws MovieDbException
     */
    public Person getPersonInfo(int personId) throws MovieDbException {

        URL url = tmdbPersonInfo.getIdUrl(personId);
        String webpage = WebBrowser.request(url);

        try {
            return mapper.readValue(webpage, Person.class);
        } catch (IOException ex) {
            LOGGER.warn("Failed to get movie info: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve all of the cast & crew information for
     * the person.
     *
     * It will return the single highest rated poster for each movie record.
     *
     * @param personId
     * @return
     * @throws MovieDbException
     */
    public List<PersonCredit> getPersonCredits(int personId) throws MovieDbException {

        List<PersonCredit> personCredits = new ArrayList<PersonCredit>();

        URL url = tmdbPersonCredits.getIdUrl(personId);
        String webpage = WebBrowser.request(url);

        try {
            WrapperPersonCredits wrapper = mapper.readValue(webpage, WrapperPersonCredits.class);

            // Add a cast member
            for (PersonCredit cast : wrapper.getCast()) {
                cast.setPersonType(PersonType.CAST);
                personCredits.add(cast);
            }
            // Add a crew member
            for (PersonCredit crew : wrapper.getCrew()) {
                crew.setPersonType(PersonType.CREW);
                personCredits.add(crew);
            }
            return personCredits;
        } catch (IOException ex) {
            LOGGER.warn("Failed to get person credits: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve all of the profile images for a person.
     *
     * @param personId
     * @return
     * @throws MovieDbException
     */
    public List<Artwork> getPersonImages(int personId) throws MovieDbException {

        List<Artwork> personImages = new ArrayList<Artwork>();

        URL url = tmdbPersonImages.getIdUrl(personId);
        String webpage = WebBrowser.request(url);

        try {
            WrapperImages wrapper = mapper.readValue(webpage, WrapperImages.class);

            // Update the image type
            for (Artwork artwork : wrapper.getProfiles()) {
                artwork.setArtworkType(ArtworkType.PROFILE);
                personImages.add(artwork);
            }
            return personImages;
        } catch (IOException ex) {
            LOGGER.warn("Failed to get person images: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve the newest movie that was added to TMDb.
     *
     * @return
     */
    public MovieDb getLatestMovie() throws MovieDbException {

        URL url = tmdbLatestMovie.getIdUrl("");
        String webpage = WebBrowser.request(url);

        try {
            return mapper.readValue(webpage, MovieDb.class);
        } catch (IOException ex) {
            LOGGER.warn("Failed to get latest movie: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * Get the list of upcoming movies.
     *
     * This list refreshes every day.
     *
     * The maximum number of items this list will include is 100.
     *
     * @return
     * @throws MovieDbException
     */
    public List<MovieDb> getUpcoming(String language) throws MovieDbException {
        URL url = tmdbUpcoming.getIdUrl("", language);
        String webpage = WebBrowser.request(url);

        try {
            WrapperMovie wrapper = mapper.readValue(webpage, WrapperMovie.class);
            return wrapper.getMovies();
        } catch (IOException ex) {
            LOGGER.warn("Failed to get upcoming movies: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }

    }

    /**
     * This method is used to retrieve the movies currently in theatres.
     *
     * This is a curated list that will normally contain 100 movies. The default
     * response will return 20 movies.
     *
     * TODO: Implement more than 20 movies
     *
     * @param language
     * @param allResults
     * @return
     * @throws MovieDbException
     */
    public List<MovieDb> getNowPlayingMovies(String language, boolean allResults) throws MovieDbException {
        URL url = tmdbNowPlaying.getIdUrl("", language);
        String webpage = WebBrowser.request(url);

        try {
            WrapperMovie wrapper = mapper.readValue(webpage, WrapperMovie.class);
            return wrapper.getMovies();
        } catch (IOException ex) {
            LOGGER.warn("Failed to get now playing movies: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve the daily movie popularity list.
     *
     * This list is updated daily. The default response will return 20 movies.
     *
     * TODO: Implement more than 20 movies
     *
     * @param language
     * @param allResults
     * @return
     * @throws MovieDbException
     */
    public List<MovieDb> getPopularMovieList(String language, boolean allResults) throws MovieDbException {
        URL url = tmdbPopularMovieList.getIdUrl("", language);
        String webpage = WebBrowser.request(url);

        try {
            WrapperMovie wrapper = mapper.readValue(webpage, WrapperMovie.class);
            return wrapper.getMovies();
        } catch (IOException ex) {
            LOGGER.warn("Failed to get popular movie list: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve the top rated movies that have over 10
     * votes on TMDb.
     *
     * The default response will return 20 movies.
     *
     * TODO: Implement more than 20 movies
     *
     * @param language
     * @param allResults
     * @return
     * @throws MovieDbException
     */
    public List<MovieDb> getTopRatedMovies(String language, boolean allResults) throws MovieDbException {
        URL url = tmdbTopRatedMovies.getIdUrl("", language);
        String webpage = WebBrowser.request(url);

        try {
            WrapperMovie wrapper = mapper.readValue(webpage, WrapperMovie.class);
            return wrapper.getMovies();
        } catch (IOException ex) {
            LOGGER.warn("Failed to get top rated movies: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve the basic information about a production
     * company on TMDb.
     *
     * @param companyId
     * @return
     * @throws MovieDbException
     */
    public Company getCompanyInfo(int companyId) throws MovieDbException {
        URL url = tmdbCompanyInfo.getIdUrl(companyId);
        String webpage = WebBrowser.request(url);

        try {
            return mapper.readValue(webpage, Company.class);
        } catch (IOException ex) {
            LOGGER.warn("Failed to get company information: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * This method is used to retrieve the movies associated with a company.
     *
     * These movies are returned in order of most recently released to oldest.
     * The default response will return 20 movies per page.
     *
     * TODO: Implement more than 20 movies
     *
     * @param companyId
     * @param language
     * @param allResults
     * @return
     * @throws MovieDbException
     */
    public List<MovieDb> getCompanyMovies(int companyId, String language, boolean allResults) throws MovieDbException {
        URL url = tmdbCompanyMovies.getIdUrl(companyId, language);
        String webpage = WebBrowser.request(url);

        try {
            WrapperCompanyMovies wrapper = mapper.readValue(webpage, WrapperCompanyMovies.class);
            return wrapper.getResults();
        } catch (IOException ex) {
            LOGGER.warn("Failed to get company movies: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * Search Companies.
     *
     * You can use this method to search for production companies that are part
     * of TMDb. The company IDs will map to those returned on movie calls.
     *
     * http://help.themoviedb.org/kb/api/search-companies
     *
     * TODO: Make the allResults work
     *
     * @param companyName
     * @param language
     * @param allResults
     * @return
     * @throws MovieDbException
     */
    public List<Company> searchCompanies(String companyName, String language, boolean allResults) throws MovieDbException {

        URL url = tmdbSearchCompanies.getQueryUrl(companyName, language, 1);
        String webpage = WebBrowser.request(url);
        try {
            WrapperCompany wrapper = mapper.readValue(webpage, WrapperCompany.class);
            return wrapper.getResults();
        } catch (IOException ex) {
            LOGGER.warn("Failed to find company: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * The similar movies method will let you retrieve the similar movies for a
     * particular movie.
     *
     * This data is created dynamically but with the help of users votes on
     * TMDb.
     *
     * The data is much better with movies that have more keywords
     *
     * @param movieId
     * @param language
     * @param allResults
     * @return
     * @throws MovieDbException
     */
    public List<MovieDb> getSimilarMovies(int movieId, String language, boolean allResults) throws MovieDbException {

        URL url = tmdbMovieSimilarMovies.getIdUrl(movieId, language);
        String webpage = WebBrowser.request(url);

        try {
            WrapperMovie wrapper = mapper.readValue(webpage, WrapperMovie.class);
            return wrapper.getMovies();
        } catch (IOException ex) {
            LOGGER.warn("Failed to get similar movies: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * You can use this method to retrieve the list of genres used on TMDb.
     *
     * These IDs will correspond to those found in movie calls.
     *
     * @param language
     * @return
     */
    public List<Genre> getGenreList(String language) throws MovieDbException {
        URL url = tmdbGenreList.getQueryUrl("", language);
        String webpage = WebBrowser.request(url);

        try {
            WrapperGenres wrapper = mapper.readValue(webpage, WrapperGenres.class);
            return wrapper.getGenres();
        } catch (IOException ex) {
            LOGGER.warn("Failed to get genre list: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }

    /**
     * Get a list of movies per genre.
     *
     * It is important to understand that only movies with more than 10 votes
     * get listed.
     *
     * This prevents movies from 1 10/10 rating from being listed first and for
     * the first 5 pages.
     *
     * @param genreId
     * @param language
     * @param allResults
     * @return
     */
    public List<MovieDb> getGenreMovies(int genreId, String language, boolean allResults) throws MovieDbException {
        URL url = tmdbGenreMovies.getIdUrl(genreId, language);
        String webpage = WebBrowser.request(url);

        try {
            WrapperMovie wrapper = mapper.readValue(webpage, WrapperMovie.class);
            return wrapper.getMovies();
        } catch (IOException ex) {
            LOGGER.warn("Failed to get genre movie list: " + ex.getMessage());
            throw new MovieDbException(MovieDbExceptionType.MAPPING_FAILED, webpage, ex);
        }
    }
}
