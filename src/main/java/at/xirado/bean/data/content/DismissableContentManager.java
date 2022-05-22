package at.xirado.bean.data.content;

import at.xirado.bean.data.database.SQLBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.*;

public class DismissableContentManager {

    private static final List<IDismissable<?>> CONTENT = new ArrayList<>();

    static {
        CONTENT.add(new RankCustomBackgroundDismissableContent());
        CONTENT.add(new BookmarkDismissableContent());
    }

    private final Map<Long, List<DismissableContentState>> STATES = Collections.synchronizedMap(new HashMap<>());

    @Nonnull
    public DismissableContentState createDismissableContent(long userId, Class<? extends IDismissable<?>> dismissable,
                                                            DismissableContentState.State initialState) {
        IDismissable<?> object = getFromClass(dismissable);
        DismissableContentState state = new DismissableContentState(userId, object, initialState);
        List<DismissableContentState> states = STATES.computeIfAbsent(userId, (k) -> new ArrayList<>());
        states.add(state);
        return state;
    }

    @Nullable
    public DismissableContentState getState(long userId, Class<? extends IDismissable<?>> dismissable, boolean retrieve) {
        return STATES.getOrDefault(userId, Collections.emptyList())
                .stream()
                .filter(state -> dismissable.isInstance(state.getContent()))
                .findFirst()
                .orElse(retrieve ? retrieveState(userId, dismissable) : null);
    }

    @Nullable
    public DismissableContentState retrieveState(long userId, Class<? extends IDismissable<?>> dismissable) {
        IDismissable<?> dismissableObj = getFromClass(dismissable);
        try (var rs = new SQLBuilder("SELECT * FROM dismissable_contents WHERE user_id = ? AND identifier = ?",
                userId, dismissableObj.getIdentifier()).executeQuery()) {
            if (rs.next()) {
                DismissableContentState.State state = DismissableContentState.State.valueOf(rs.getString("state"));
                return new DismissableContentState(userId, dismissableObj, state);
            }
            return null;
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    public boolean hasState(long userId, Class<? extends IDismissable<?>> dismissable) {
        return getState(userId, dismissable, true) != null;
    }

    @Nonnull
    public IDismissable<?> getFromIdentifier(String identifier) {
        return CONTENT.stream()
                .filter(dismissable -> dismissable.getIdentifier().equals(identifier))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No Dismissable with identifier \"" + identifier + "\" found!"));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public Class<? extends IDismissable<?>> getClassFromIdentifier(String identifier) {
        return (Class<? extends IDismissable<?>>) CONTENT.stream()
                .filter(dismissable -> dismissable.getIdentifier().equals(identifier))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No Dismissable with identifier \"" + identifier + "\" found!")).getClass();
    }

    @Nonnull
    public IDismissable<?> getFromClass(Class<? extends IDismissable<?>> clazz) {
        return CONTENT.stream()
                .filter(clazz::isInstance)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Passed IDismissable is not registered!"));
    }
}
