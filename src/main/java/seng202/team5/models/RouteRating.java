package seng202.team5.models;

/**
 * Like and dislike totals for a route
 * @param likes the current like count for the route
 * @param dislikes the current dislike count for the route
 * @param userVote the current like/dislike/non-vote the user has for the route
 */
public record RouteRating(int likes, int dislikes, int userVote) {

    public final static RouteRating NONE = new RouteRating(0, 0, 0);

    public RouteRating toggle(int clicked) {
        int newVote = userVote == clicked ? 0 : clicked;
        int newLikes = likes - (userVote == 1 ? 1 : 0) + (newVote == 1 ? 1 : 0);
        int newDisLikes = dislikes - (userVote == -1 ? 1 : 0) + (newVote == -1 ? 1 : 0);
        return  new RouteRating(newLikes, newDisLikes, newVote);
    }

    public RouteRating voteChange(int newVote) {

        if (userVote == newVote) {
            return this;
        }

        int newLikes = likes - (userVote == 1 ? 1 : 0) + (newVote == 1 ? 1 : 0);
        int newDislikes = dislikes - (userVote == -1 ? 1 : 0) + (newVote == -1 ? 1 : 0);
        return new RouteRating(newLikes, newDislikes, newVote);
    }
}
