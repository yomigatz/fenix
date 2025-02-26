/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components.appstate

import androidx.annotation.VisibleForTesting
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.ext.filterOutTab
import org.mozilla.fenix.ext.getFilteredStories
import org.mozilla.fenix.ext.recentSearchGroup
import org.mozilla.fenix.home.pocket.POCKET_STORIES_TO_SHOW_COUNT
import org.mozilla.fenix.home.pocket.PocketRecommendedStoriesSelectedCategory
import org.mozilla.fenix.home.recenttabs.RecentTab
import org.mozilla.fenix.home.recentvisits.RecentlyVisitedItem
import org.mozilla.fenix.home.recentvisits.RecentlyVisitedItem.RecentHistoryGroup
import org.mozilla.fenix.gleanplumb.state.MessagingReducer

/**
 * Reducer for [AppStore].
 */
internal object AppStoreReducer {
    @Suppress("LongMethod")
    fun reduce(state: AppState, action: AppAction): AppState = when (action) {
        is AppAction.UpdateInactiveExpanded ->
            state.copy(inactiveTabsExpanded = action.expanded)
        is AppAction.AddNonFatalCrash ->
            state.copy(nonFatalCrashes = state.nonFatalCrashes + action.crash)
        is AppAction.RemoveNonFatalCrash ->
            state.copy(nonFatalCrashes = state.nonFatalCrashes - action.crash)
        is AppAction.RemoveAllNonFatalCrashes ->
            state.copy(nonFatalCrashes = emptyList())

        is AppAction.MessagingAction -> MessagingReducer.reduce(state, action)

        is AppAction.Change -> state.copy(
            collections = action.collections,
            mode = action.mode,
            topSites = action.topSites,
            recentBookmarks = action.recentBookmarks,
            recentTabs = action.recentTabs,
            recentHistory = if (action.recentHistory.isNotEmpty() && action.recentTabs.isNotEmpty()) {
                val recentSearchGroup = action.recentTabs.find { it is RecentTab.SearchGroup } as RecentTab.SearchGroup?
                action.recentHistory.filterOut(recentSearchGroup?.searchTerm)
            } else {
                action.recentHistory
            }
        )
        is AppAction.CollectionExpanded -> {
            val newExpandedCollection = state.expandedCollections.toMutableSet()

            if (action.expand) {
                newExpandedCollection.add(action.collection.id)
            } else {
                newExpandedCollection.remove(action.collection.id)
            }

            state.copy(expandedCollections = newExpandedCollection)
        }
        is AppAction.CollectionsChange -> state.copy(collections = action.collections)
        is AppAction.ModeChange -> state.copy(mode = action.mode)
        is AppAction.TopSitesChange -> state.copy(topSites = action.topSites)
        is AppAction.RemoveCollectionsPlaceholder -> {
            state.copy(showCollectionPlaceholder = false)
        }
        is AppAction.RecentTabsChange -> {
            val recentSearchGroup = action.recentTabs.find { it is RecentTab.SearchGroup } as RecentTab.SearchGroup?
            state.copy(
                recentTabs = action.recentTabs,
                recentHistory = state.recentHistory.filterOut(recentSearchGroup?.searchTerm)
            )
        }
        is AppAction.RemoveRecentTab -> {
            state.copy(
                recentTabs = state.recentTabs.filterOutTab(action.recentTab)
            )
        }
        is AppAction.RecentBookmarksChange -> state.copy(recentBookmarks = action.recentBookmarks)
        is AppAction.RemoveRecentBookmark -> {
            state.copy(recentBookmarks = state.recentBookmarks.filterNot { it.url == action.recentBookmark.url })
        }
        is AppAction.RecentHistoryChange -> state.copy(
            recentHistory = action.recentHistory.filterOut(state.recentSearchGroup?.searchTerm)
        )
        is AppAction.RemoveRecentHistoryHighlight -> state.copy(
            recentHistory = state.recentHistory.filterNot {
                it is RecentlyVisitedItem.RecentHistoryHighlight && it.url == action.highlightUrl
            }
        )
        is AppAction.DisbandSearchGroupAction -> state.copy(
            recentHistory = state.recentHistory.filterNot {
                it is RecentlyVisitedItem.RecentHistoryGroup && (
                    it.title.equals(action.searchTerm, true) ||
                        it.title.equals(state.recentSearchGroup?.searchTerm, true)
                    )
            }
        )
        is AppAction.SelectPocketStoriesCategory -> {
            val updatedCategoriesState = state.copy(
                pocketStoriesCategoriesSelections =
                state.pocketStoriesCategoriesSelections + PocketRecommendedStoriesSelectedCategory(
                    name = action.categoryName
                )
            )

            // Selecting a category means the stories to be displayed needs to also be changed.
            updatedCategoriesState.copy(
                pocketStories = updatedCategoriesState.getFilteredStories(
                    POCKET_STORIES_TO_SHOW_COUNT
                )
            )
        }
        is AppAction.DeselectPocketStoriesCategory -> {
            val updatedCategoriesState = state.copy(
                pocketStoriesCategoriesSelections = state.pocketStoriesCategoriesSelections.filterNot {
                    it.name == action.categoryName
                }
            )

            // Deselecting a category means the stories to be displayed needs to also be changed.
            updatedCategoriesState.copy(
                pocketStories = updatedCategoriesState.getFilteredStories(
                    POCKET_STORIES_TO_SHOW_COUNT
                )
            )
        }
        is AppAction.PocketStoriesCategoriesChange -> {
            val updatedCategoriesState = state.copy(pocketStoriesCategories = action.storiesCategories)
            // Whenever categories change stories to be displayed needs to also be changed.
            updatedCategoriesState.copy(
                pocketStories = updatedCategoriesState.getFilteredStories(
                    POCKET_STORIES_TO_SHOW_COUNT
                )
            )
        }
        is AppAction.PocketStoriesCategoriesSelectionsChange -> {
            val updatedCategoriesState = state.copy(
                pocketStoriesCategories = action.storiesCategories,
                pocketStoriesCategoriesSelections = action.categoriesSelected
            )
            // Whenever categories change stories to be displayed needs to also be changed.
            updatedCategoriesState.copy(
                pocketStories = updatedCategoriesState.getFilteredStories(
                    POCKET_STORIES_TO_SHOW_COUNT
                )
            )
        }
        is AppAction.PocketStoriesChange -> state.copy(pocketStories = action.pocketStories)
        is AppAction.PocketStoriesShown -> {
            var updatedCategories = state.pocketStoriesCategories
            action.storiesShown.forEach { shownStory ->
                updatedCategories = updatedCategories.map { category ->
                    when (category.name == shownStory.category) {
                        true -> {
                            category.copy(
                                stories = category.stories.map { story ->
                                    when (story.title == shownStory.title) {
                                        true -> story.copy(timesShown = story.timesShown.inc())
                                        false -> story
                                    }
                                }
                            )
                        }
                        false -> category
                    }
                }
            }

            state.copy(pocketStoriesCategories = updatedCategories)
        }
    }
}

/**
 * Removes a [RecentHistoryGroup] identified by [groupTitle] if it exists in the current list.
 *
 * @param groupTitle [RecentHistoryGroup.title] of the item that should be removed.
 */
@VisibleForTesting
internal fun List<RecentlyVisitedItem>.filterOut(groupTitle: String?): List<RecentlyVisitedItem> {
    return when (groupTitle != null) {
        true -> filterNot { it is RecentHistoryGroup && it.title.equals(groupTitle, true) }
        false -> this
    }
}
