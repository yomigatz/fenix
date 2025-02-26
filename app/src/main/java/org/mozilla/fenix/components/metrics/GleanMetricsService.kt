/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components.metrics

import android.content.Context
import mozilla.components.service.glean.Glean
import mozilla.components.service.glean.private.NoExtraKeys
import mozilla.components.support.base.log.logger.Logger
import org.mozilla.fenix.GleanMetrics.Addons
import org.mozilla.fenix.GleanMetrics.AndroidAutofill
import org.mozilla.fenix.GleanMetrics.AppTheme
import org.mozilla.fenix.GleanMetrics.Autoplay
import org.mozilla.fenix.GleanMetrics.Awesomebar
import org.mozilla.fenix.GleanMetrics.BrowserSearch
import org.mozilla.fenix.GleanMetrics.ContextMenu
import org.mozilla.fenix.GleanMetrics.ContextualMenu
import org.mozilla.fenix.GleanMetrics.CreditCards
import org.mozilla.fenix.GleanMetrics.CustomTab
import org.mozilla.fenix.GleanMetrics.Events
import org.mozilla.fenix.GleanMetrics.ExperimentsDefaultBrowser
import org.mozilla.fenix.GleanMetrics.History
import org.mozilla.fenix.GleanMetrics.HomeMenu
import org.mozilla.fenix.GleanMetrics.HomeScreen
import org.mozilla.fenix.GleanMetrics.Logins
import org.mozilla.fenix.GleanMetrics.MediaNotification
import org.mozilla.fenix.GleanMetrics.MediaState
import org.mozilla.fenix.GleanMetrics.Metrics
import org.mozilla.fenix.GleanMetrics.Pings
import org.mozilla.fenix.GleanMetrics.Pocket
import org.mozilla.fenix.GleanMetrics.ProgressiveWebApp
import org.mozilla.fenix.GleanMetrics.ReaderMode
import org.mozilla.fenix.GleanMetrics.RecentBookmarks
import org.mozilla.fenix.GleanMetrics.RecentSearches
import org.mozilla.fenix.GleanMetrics.RecentTabs
import org.mozilla.fenix.GleanMetrics.RecentlyVisitedHomepage
import org.mozilla.fenix.GleanMetrics.SearchTerms
import org.mozilla.fenix.GleanMetrics.StartOnHome
import org.mozilla.fenix.GleanMetrics.SyncedTabs
import org.mozilla.fenix.GleanMetrics.Tab
import org.mozilla.fenix.GleanMetrics.Tabs
import org.mozilla.fenix.GleanMetrics.TopSites
import org.mozilla.fenix.GleanMetrics.VoiceSearch
import org.mozilla.fenix.GleanMetrics.Wallpapers
import org.mozilla.fenix.GleanMetrics.Messaging
import org.mozilla.fenix.ext.components

private class EventWrapper<T : Enum<T>>(
    private val recorder: ((Map<T, String>?) -> Unit),
    private val keyMapper: ((String) -> T)? = null
) {

    /**
     * Converts snake_case string to camelCase.
     */
    private fun String.asCamelCase(): String {
        val parts = split("_")
        val builder = StringBuilder()

        for ((index, part) in parts.withIndex()) {
            if (index == 0) {
                builder.append(part)
            } else {
                builder.append(part[0].uppercase())
                builder.append(part.substring(1))
            }
        }

        return builder.toString()
    }

    fun track(event: Event) {
        val extras = if (keyMapper != null) {
            event.extras?.mapKeys { (key) ->
                keyMapper.invoke(key.toString().asCamelCase())
            }
        } else {
            null
        }

        @Suppress("DEPRECATION")
        // FIXME(#19967): Migrate to non-deprecated API.
        this.recorder(extras)
    }
}

@Suppress("DEPRECATION")
// FIXME(#19967): Migrate to non-deprecated API.
private val Event.wrapper: EventWrapper<*>?
    get() = when (this) {
        is Event.PerformedSearch -> EventWrapper(
            {
                Metrics.searchCount[this.eventSource.countLabel].add(1)
                Events.performedSearch.record(it)
            },
            { Events.performedSearchKeys.valueOf(it) }
        )
        is Event.SearchWithAds -> EventWrapper<NoExtraKeys>(
            {
                BrowserSearch.withAds[label].add(1)
            }
        )
        is Event.SearchAdClicked -> EventWrapper<NoExtraKeys>(
            {
                BrowserSearch.adClicks[label].add(1)
            }
        )
        is Event.SearchInContent -> EventWrapper<NoExtraKeys>(
            {
                BrowserSearch.inContent[label].add(1)
            }
        )
        is Event.ContextMenuItemTapped -> EventWrapper(
            { ContextMenu.itemTapped.record(it) },
            { ContextMenu.itemTappedKeys.valueOf(it) }
        )

        is Event.SetDefaultBrowserToolbarMenuClicked -> EventWrapper<NoExtraKeys>(
            { ExperimentsDefaultBrowser.toolbarMenuClicked.record(it) }
        )

        is Event.CustomTabsMenuOpened -> EventWrapper<NoExtraKeys>(
            { CustomTab.menu.record(it) }
        )
        is Event.CustomTabsActionTapped -> EventWrapper<NoExtraKeys>(
            { CustomTab.actionButton.record(it) }
        )
        is Event.CustomTabsClosed -> EventWrapper<NoExtraKeys>(
            { CustomTab.closed.record(it) }
        )
        is Event.HistoryOpened -> EventWrapper<NoExtraKeys>(
            { History.opened.record(it) }
        )
        is Event.HistoryItemShared -> EventWrapper<NoExtraKeys>(
            { History.shared.record(it) }
        )
        is Event.HistoryItemOpened -> EventWrapper<NoExtraKeys>(
            { History.openedItem.record(it) }
        )
        is Event.HistoryOpenedInNewTab -> EventWrapper<NoExtraKeys>(
            { History.openedItemInNewTab.record(it) }
        )
        is Event.HistoryOpenedInNewTabs -> EventWrapper<NoExtraKeys>(
            { History.openedItemsInNewTabs.record(it) }
        )
        is Event.HistoryOpenedInPrivateTab -> EventWrapper<NoExtraKeys>(
            { History.openedItemInPrivateTab.record(it) }
        )
        is Event.HistoryOpenedInPrivateTabs -> EventWrapper<NoExtraKeys>(
            { History.openedItemsInPrivateTabs.record(it) }
        )
        is Event.HistoryItemRemoved -> EventWrapper<NoExtraKeys>(
            { History.removed.record(it) }
        )
        is Event.HistoryAllItemsRemoved -> EventWrapper<NoExtraKeys>(
            { History.removedAll.record(it) }
        )
        is Event.HistoryRecentSearchesTapped -> EventWrapper(
            { History.recentSearchesTapped.record(it) },
            { History.recentSearchesTappedKeys.valueOf(it) }
        )
        is Event.HistorySearchTermGroupTapped -> EventWrapper<NoExtraKeys>(
            { History.searchTermGroupTapped.record(it) }
        )
        is Event.HistorySearchTermGroupOpenTab -> EventWrapper<NoExtraKeys>(
            { History.searchTermGroupOpenTab.record(it) }
        )
        is Event.HistorySearchTermGroupRemoveTab -> EventWrapper<NoExtraKeys>(
            { History.searchTermGroupRemoveTab.record(it) }
        )
        is Event.HistorySearchTermGroupRemoveAll -> EventWrapper<NoExtraKeys>(
            { History.searchTermGroupRemoveAll.record(it) }
        )
        is Event.HistorySearchIconTapped -> EventWrapper<NoExtraKeys>(
            { History.searchIconTapped.record(it) }
        )
        is Event.HistorySearchResultTapped -> EventWrapper<NoExtraKeys>(
            { History.searchResultTapped.record(it) }
        )
        is Event.ReaderModeAvailable -> EventWrapper<NoExtraKeys>(
            { ReaderMode.available.record(it) }
        )
        is Event.ReaderModeOpened -> EventWrapper<NoExtraKeys>(
            { ReaderMode.opened.record(it) }
        )
        is Event.ReaderModeClosed -> EventWrapper<NoExtraKeys>(
            { ReaderMode.closed.record(it) }
        )
        is Event.ReaderModeAppearanceOpened -> EventWrapper<NoExtraKeys>(
            { ReaderMode.appearance.record(it) }
        )
        is Event.TabMediaPlay -> EventWrapper<NoExtraKeys>(
            { Tab.mediaPlay.record(it) }
        )
        is Event.TabMediaPause -> EventWrapper<NoExtraKeys>(
            { Tab.mediaPause.record(it) }
        )
        is Event.MediaPlayState -> EventWrapper<NoExtraKeys>(
            { MediaState.play.record(it) }
        )
        is Event.MediaPauseState -> EventWrapper<NoExtraKeys>(
            { MediaState.pause.record(it) }
        )
        is Event.MediaStopState -> EventWrapper<NoExtraKeys>(
            { MediaState.stop.record(it) }
        )
        is Event.MediaFullscreenState -> EventWrapper<NoExtraKeys>(
            { MediaState.fullscreen.record(it) }
        )
        is Event.MediaPictureInPictureState -> EventWrapper<NoExtraKeys>(
            { MediaState.pictureInPicture.record(it) }
        )
        is Event.NotificationMediaPlay -> EventWrapper<NoExtraKeys>(
            { MediaNotification.play.record(it) }
        )
        is Event.NotificationMediaPause -> EventWrapper<NoExtraKeys>(
            { MediaNotification.pause.record(it) }
        )
        is Event.OpenLogins -> EventWrapper<NoExtraKeys>(
            { Logins.openLogins.record(it) }
        )
        is Event.OpenOneLogin -> EventWrapper<NoExtraKeys>(
            { Logins.openIndividualLogin.record(it) }
        )
        is Event.CopyLogin -> EventWrapper<NoExtraKeys>(
            { Logins.copyLogin.record(it) }
        )
        is Event.ViewLoginPassword -> EventWrapper<NoExtraKeys>(
            { Logins.viewPasswordLogin.record(it) }
        )
        is Event.DeleteLogin -> EventWrapper<NoExtraKeys>(
            { Logins.deleteSavedLogin.record(it) }
        )
        is Event.EditLogin -> EventWrapper<NoExtraKeys>(
            { Logins.openLoginEditor.record(it) }
        )
        is Event.EditLoginSave -> EventWrapper<NoExtraKeys>(
            { Logins.saveEditedLogin.record(it) }
        )
        is Event.SaveLoginsSettingChanged -> EventWrapper(
            { Logins.saveLoginsSettingChanged.record(it) },
            { Logins.saveLoginsSettingChangedKeys.valueOf(it) }
        )
        is Event.TopSiteOpenDefault -> EventWrapper<NoExtraKeys>(
            { TopSites.openDefault.record(it) }
        )
        is Event.TopSiteOpenGoogle -> EventWrapper<NoExtraKeys>(
            { TopSites.openGoogleSearchAttribution.record(it) }
        )
        is Event.TopSiteOpenBaidu -> EventWrapper<NoExtraKeys>(
            { TopSites.openBaiduSearchAttribution.record(it) }
        )
        is Event.TopSiteOpenFrecent -> EventWrapper<NoExtraKeys>(
            { TopSites.openFrecency.record(it) }
        )
        is Event.TopSiteOpenPinned -> EventWrapper<NoExtraKeys>(
            { TopSites.openPinned.record(it) }
        )
        is Event.TopSiteOpenProvided -> EventWrapper<NoExtraKeys>(
            { TopSites.openContileTopSite.record(it) }
        )
        is Event.TopSiteOpenInNewTab -> EventWrapper<NoExtraKeys>(
            { TopSites.openInNewTab.record(it) }
        )
        is Event.TopSiteOpenInPrivateTab -> EventWrapper<NoExtraKeys>(
            { TopSites.openInPrivateTab.record(it) }
        )
        is Event.TopSiteOpenContileInPrivateTab -> EventWrapper<NoExtraKeys>(
            { TopSites.openContileInPrivateTab.record(it) }
        )
        is Event.TopSiteRemoved -> EventWrapper<NoExtraKeys>(
            { TopSites.remove.record(it) }
        )
        is Event.TopSiteContileSettings -> EventWrapper<NoExtraKeys>(
            { TopSites.contileSettings.record(it) }
        )
        is Event.TopSiteContilePrivacy -> EventWrapper<NoExtraKeys>(
            { TopSites.contileSponsorsAndPrivacy.record(it) }
        )
        is Event.GoogleTopSiteRemoved -> EventWrapper<NoExtraKeys>(
            { TopSites.googleTopSiteRemoved.record(it) }
        )
        is Event.BaiduTopSiteRemoved -> EventWrapper<NoExtraKeys>(
            { TopSites.baiduTopSiteRemoved.record(it) }
        )
        is Event.TopSiteLongPress -> EventWrapper(
            { TopSites.longPress.record(it) },
            { TopSites.longPressKeys.valueOf(it) }
        )
        is Event.TopSiteSwipeCarousel -> EventWrapper(
            { TopSites.swipeCarousel.record(it) },
            { TopSites.swipeCarouselKeys.valueOf(it) }
        )
        is Event.TopSiteContileImpression -> EventWrapper<NoExtraKeys>(
            {
                TopSites.contileImpression.record(
                    TopSites.ContileImpressionExtra(
                        position = this.position,
                        source = this.source.name.lowercase()
                    )
                )
            }
        )
        is Event.TopSiteContileClick -> EventWrapper<NoExtraKeys>(
            {
                TopSites.contileClick.record(
                    TopSites.ContileClickExtra(
                        position = this.position,
                        source = this.source.name.lowercase()
                    )
                )
            }
        )
        is Event.PocketTopSiteClicked -> EventWrapper<NoExtraKeys>(
            { Pocket.pocketTopSiteClicked.record(it) }
        )
        is Event.PocketTopSiteRemoved -> EventWrapper<NoExtraKeys>(
            { Pocket.pocketTopSiteRemoved.record(it) }
        )
        is Event.PocketHomeRecsShown -> EventWrapper<NoExtraKeys>(
            { Pocket.homeRecsShown.record(it) }
        )
        is Event.PocketHomeRecsLearnMoreClicked -> EventWrapper<NoExtraKeys>(
            { Pocket.homeRecsLearnMoreClicked.record(it) }
        )
        is Event.PocketHomeRecsDiscoverMoreClicked -> EventWrapper<NoExtraKeys>(
            { Pocket.homeRecsDiscoverClicked.record(it) }
        )
        is Event.PocketHomeRecsStoryClicked -> EventWrapper(
            { Pocket.homeRecsStoryClicked.record(it) },
            { Pocket.homeRecsStoryClickedKeys.valueOf(it) }
        )
        is Event.PocketHomeRecsCategoryClicked -> EventWrapper(
            { Pocket.homeRecsCategoryClicked.record(it) },
            { Pocket.homeRecsCategoryClickedKeys.valueOf(it) }
        )
        is Event.DarkThemeSelected -> EventWrapper(
            { AppTheme.darkThemeSelected.record(it) },
            { AppTheme.darkThemeSelectedKeys.valueOf(it) }
        )
        is Event.AddonsOpenInSettings -> EventWrapper<NoExtraKeys>(
            { Addons.openAddonsInSettings.record(it) }
        )
        is Event.AddonsOpenInToolbarMenu -> EventWrapper(
            { Addons.openAddonInToolbarMenu.record(it) },
            { Addons.openAddonInToolbarMenuKeys.valueOf(it) }
        )
        is Event.AddonOpenSetting -> EventWrapper(
            { Addons.openAddonSetting.record(it) },
            { Addons.openAddonSettingKeys.valueOf(it) }
        )
        is Event.VoiceSearchTapped -> EventWrapper<NoExtraKeys>(
            { VoiceSearch.tapped.record(it) }
        )

        is Event.AutoPlaySettingVisited -> EventWrapper<NoExtraKeys>(
            { Autoplay.visitedSetting.record(it) }
        )
        is Event.AutoPlaySettingChanged -> EventWrapper(
            { Autoplay.settingChanged.record(it) },
            { Autoplay.settingChangedKeys.valueOf(it) }
        )
        is Event.ProgressiveWebAppOpenFromHomescreenTap -> EventWrapper<NoExtraKeys>(
            { ProgressiveWebApp.homescreenTap.record(it) }
        )
        is Event.ProgressiveWebAppInstallAsShortcut -> EventWrapper<NoExtraKeys>(
            { ProgressiveWebApp.installTap.record(it) }
        )

        is Event.TabSettingsOpened -> EventWrapper<NoExtraKeys>(
            { Tabs.settingOpened.record(it) }
        )
        Event.ContextMenuCopyTapped -> EventWrapper<NoExtraKeys>(
            { ContextualMenu.copyTapped.record(it) }
        )
        is Event.ContextMenuSearchTapped -> EventWrapper<NoExtraKeys>(
            { ContextualMenu.searchTapped.record(it) }
        )
        is Event.ContextMenuSelectAllTapped -> EventWrapper<NoExtraKeys>(
            { ContextualMenu.selectAllTapped.record(it) }
        )
        is Event.ContextMenuShareTapped -> EventWrapper<NoExtraKeys>(
            { ContextualMenu.shareTapped.record(it) }
        )
        Event.HaveOpenTabs -> EventWrapper<NoExtraKeys>(
            { Metrics.hasOpenTabs.set(true) }
        )
        Event.HaveNoOpenTabs -> EventWrapper<NoExtraKeys>(
            { Metrics.hasOpenTabs.set(false) }
        )
        is Event.SyncedTabSuggestionClicked -> EventWrapper<NoExtraKeys>(
            { SyncedTabs.syncedTabsSuggestionClicked.record(it) }
        )

        is Event.BookmarkSuggestionClicked -> EventWrapper<NoExtraKeys>(
            { Awesomebar.bookmarkSuggestionClicked.record(it) }
        )
        is Event.ClipboardSuggestionClicked -> EventWrapper<NoExtraKeys>(
            { Awesomebar.clipboardSuggestionClicked.record(it) }
        )
        is Event.HistorySuggestionClicked -> EventWrapper<NoExtraKeys>(
            { Awesomebar.historySuggestionClicked.record(it) }
        )
        is Event.SearchActionClicked -> EventWrapper<NoExtraKeys>(
            { Awesomebar.searchActionClicked.record(it) }
        )
        is Event.SearchSuggestionClicked -> EventWrapper<NoExtraKeys>(
            { Awesomebar.searchSuggestionClicked.record(it) }
        )
        is Event.OpenedTabSuggestionClicked -> EventWrapper<NoExtraKeys>(
            { Awesomebar.openedTabSuggestionClicked.record(it) }
        )

        is Event.HomeMenuSettingsItemClicked -> EventWrapper<NoExtraKeys>(
            { HomeMenu.settingsItemClicked.record(it) }
        )

        is Event.HomeScreenDisplayed -> EventWrapper<NoExtraKeys>(
            { HomeScreen.homeScreenDisplayed.record(it) }
        )
        is Event.HomeScreenViewCount -> EventWrapper<NoExtraKeys>(
            { HomeScreen.homeScreenViewCount.add() }
        )
        is Event.HomeScreenCustomizedHomeClicked -> EventWrapper<NoExtraKeys>(
            { HomeScreen.customizeHomeClicked.record(it) }
        )
        is Event.StartOnHomeEnterHomeScreen -> EventWrapper<NoExtraKeys>(
            { StartOnHome.enterHomeScreen.record(it) }
        )

        is Event.StartOnHomeOpenTabsTray -> EventWrapper<NoExtraKeys>(
            { StartOnHome.openTabsTray.record(it) }
        )

        is Event.OpenRecentTab -> EventWrapper<NoExtraKeys>(
            { RecentTabs.recentTabOpened.record(it) }
        )

        is Event.OpenInProgressMediaTab -> EventWrapper<NoExtraKeys>(
            { RecentTabs.inProgressMediaTabOpened.record(it) }
        )

        is Event.ShowAllRecentTabs -> EventWrapper<NoExtraKeys>(
            { RecentTabs.showAllClicked.record(it) }
        )

        is Event.RecentTabsSectionIsVisible -> EventWrapper<NoExtraKeys>(
            { RecentTabs.sectionVisible.set(true) }
        )

        is Event.RecentTabsSectionIsNotVisible -> EventWrapper<NoExtraKeys>(
            { RecentTabs.sectionVisible.set(false) }
        )

        is Event.BookmarkClicked -> EventWrapper<NoExtraKeys>(
            { RecentBookmarks.bookmarkClicked.add() }
        )

        is Event.ShowAllBookmarks -> EventWrapper<NoExtraKeys>(
            { RecentBookmarks.showAllBookmarks.add() }
        )

        is Event.RecentSearchesGroupDeleted -> EventWrapper<NoExtraKeys>(
            { RecentSearches.groupDeleted.record(it) }
        )

        is Event.RecentBookmarksShown -> EventWrapper<NoExtraKeys>(
            { RecentBookmarks.shown.record(it) }
        )

        is Event.RecentBookmarkCount -> EventWrapper<NoExtraKeys>(
            { RecentBookmarks.recentBookmarksCount.set(this.count.toLong()) },
        )

        is Event.AndroidAutofillRequestWithLogins -> EventWrapper<NoExtraKeys>(
            { AndroidAutofill.requestMatchingLogins.record(it) }
        )
        is Event.AndroidAutofillRequestWithoutLogins -> EventWrapper<NoExtraKeys>(
            { AndroidAutofill.requestNoMatchingLogins.record(it) }
        )
        is Event.AndroidAutofillSearchDisplayed -> EventWrapper<NoExtraKeys>(
            { AndroidAutofill.searchDisplayed.record(it) }
        )
        is Event.AndroidAutofillSearchItemSelected -> EventWrapper<NoExtraKeys>(
            { AndroidAutofill.searchItemSelected.record(it) }
        )
        is Event.AndroidAutofillUnlockCanceled -> EventWrapper<NoExtraKeys>(
            { AndroidAutofill.unlockCancelled.record(it) }
        )
        is Event.AndroidAutofillUnlockSuccessful -> EventWrapper<NoExtraKeys>(
            { AndroidAutofill.unlockSuccessful.record(it) }
        )
        is Event.AndroidAutofillConfirmationCanceled -> EventWrapper<NoExtraKeys>(
            { AndroidAutofill.confirmCancelled.record(it) }
        )
        is Event.AndroidAutofillConfirmationSuccessful -> EventWrapper<NoExtraKeys>(
            { AndroidAutofill.confirmSuccessful.record(it) }
        )
        is Event.CreditCardSaved -> EventWrapper<NoExtraKeys>(
            { CreditCards.saved.add() }
        )
        is Event.CreditCardDeleted -> EventWrapper<NoExtraKeys>(
            { CreditCards.deleted.add() }
        )
        is Event.CreditCardModified -> EventWrapper<NoExtraKeys>(
            { CreditCards.modified.record(it) }
        )
        is Event.CreditCardFormDetected -> EventWrapper<NoExtraKeys>(
            { CreditCards.formDetected.record(it) }
        )
        is Event.CreditCardAutofillPromptShown -> EventWrapper<NoExtraKeys>(
            { CreditCards.autofillPromptShown.record(it) }
        )
        is Event.CreditCardAutofillPromptExpanded -> EventWrapper<NoExtraKeys>(
            { CreditCards.autofillPromptExpanded.record(it) }
        )
        is Event.CreditCardAutofillPromptDismissed -> EventWrapper<NoExtraKeys>(
            { CreditCards.autofillPromptDismissed.record(it) }
        )
        is Event.CreditCardAutofilled -> EventWrapper<NoExtraKeys>(
            { CreditCards.autofilled.record(it) }
        )
        is Event.CreditCardManagementAddTapped -> EventWrapper<NoExtraKeys>(
            { CreditCards.managementAddTapped.record(it) }
        )
        is Event.CreditCardManagementCardTapped -> EventWrapper<NoExtraKeys>(
            { CreditCards.managementCardTapped.record(it) }
        )
        is Event.SearchTermGroupCount -> EventWrapper(
            { SearchTerms.numberOfSearchTermGroup.record(it) },
            { SearchTerms.numberOfSearchTermGroupKeys.valueOf(it) }
        )
        is Event.AverageTabsPerSearchTermGroup -> EventWrapper(
            { SearchTerms.averageTabsPerGroup.record(it) },
            { SearchTerms.averageTabsPerGroupKeys.valueOf(it) }
        )
        is Event.SearchTermGroupSizeDistribution -> EventWrapper<NoExtraKeys>(
            { SearchTerms.groupSizeDistribution.accumulateSamples(this.groupSizes.toLongArray()) },
        )
        is Event.JumpBackInGroupTapped -> EventWrapper<NoExtraKeys>(
            { SearchTerms.jumpBackInGroupTapped.record(it) }
        )
        is Event.Messaging.MessageShown -> EventWrapper<NoExtraKeys>(
            {
                Messaging.messageShown.record(
                    Messaging.MessageShownExtra(
                        messageKey = this.messageId
                    )
                )
            }
        )
        is Event.Messaging.MessageClicked -> EventWrapper<NoExtraKeys>(
            {
                Messaging.messageClicked.record(
                    Messaging.MessageClickedExtra(
                        messageKey = this.messageId,
                        actionUuid = this.uuid
                    )
                )
            }
        )
        is Event.Messaging.MessageDismissed -> EventWrapper<NoExtraKeys>(
            {
                Messaging.messageDismissed.record(
                    Messaging.MessageDismissedExtra(
                        messageKey = this.messageId
                    )
                )
            }
        )
        is Event.Messaging.MessageMalformed -> EventWrapper<NoExtraKeys>(
            {
                Messaging.malformed.record(
                    Messaging.MalformedExtra(
                        messageKey = this.messageId
                    )
                )
            }
        )
        is Event.Messaging.MessageExpired -> EventWrapper<NoExtraKeys>(
            {
                Messaging.messageExpired.record(
                    Messaging.MessageExpiredExtra(
                        messageKey = this.messageId
                    )
                )
            }
        )
        is Event.WallpaperSettingsOpened -> EventWrapper<NoExtraKeys>(
            { Wallpapers.wallpaperSettingsOpened.record() }
        )
        is Event.WallpaperSelected -> EventWrapper<NoExtraKeys>(
            {
                Wallpapers.wallpaperSelected.record(
                    Wallpapers.WallpaperSelectedExtra(
                        name = this.wallpaper.name,
                        themeCollection = this.wallpaper::class.simpleName,
                    ),
                )
            }
        )
        is Event.WallpaperSwitched -> EventWrapper<NoExtraKeys>(
            {
                Wallpapers.wallpaperSwitched.record(
                    Wallpapers.WallpaperSwitchedExtra(
                        name = this.wallpaper.name,
                        themeCollection = this.wallpaper::class.simpleName,
                    ),
                )
            }
        )
        is Event.ChangeWallpaperWithLogoToggled -> EventWrapper<NoExtraKeys>(
            {
                Wallpapers.changeWallpaperLogoToggled.record(
                    Wallpapers.ChangeWallpaperLogoToggledExtra(
                        checked = this.checked,
                    ),
                )
            }
        )

        is Event.HistoryHighlightOpened -> EventWrapper<NoExtraKeys>(
            { RecentlyVisitedHomepage.historyHighlightOpened.record() }
        )
        is Event.HistorySearchGroupOpened -> EventWrapper<NoExtraKeys>(
            { RecentlyVisitedHomepage.searchGroupOpened.record() }
        )

        // Don't record other events in Glean:
        is Event.AddBookmark -> null
        is Event.OpenedAppFirstRun -> null
        is Event.InteractWithSearchURLArea -> null
        is Event.ClearedPrivateData -> null
        is Event.DismissedOnboarding -> null
        is Event.AddonInstalled -> null
        is Event.SearchWidgetInstalled -> null
    }

/**
 * Service responsible for sending the activation and installation pings.
 */
class GleanMetricsService(
    private val context: Context
) : MetricsService {
    override val type = MetricServiceType.Data

    private val logger = Logger("GleanMetricsService")
    private var initialized = false

    private val activationPing = ActivationPing(context)
    private val installationPing = FirstSessionPing(context)

    override fun start() {
        logger.debug("Enabling Glean.")
        // Initialization of Glean already happened in FenixApplication.
        Glean.setUploadEnabled(true)

        if (initialized) return
        initialized = true

        // The code below doesn't need to execute immediately, so we'll add them to the visual
        // completeness task queue to be run later.
        context.components.performance.visualCompletenessQueue.queue.runIfReadyOrQueue {
            // We have to initialize Glean *on* the main thread, because it registers lifecycle
            // observers. However, the activation ping must be sent *off* of the main thread,
            // because it calls Google ad APIs that must be called *off* of the main thread.
            // These two things actually happen in parallel, but that should be ok because Glean
            // can handle events being recorded before it's initialized.
            Glean.registerPings(Pings)

            activationPing.checkAndSend()
            installationPing.checkAndSend()
        }
    }

    override fun stop() {
        Glean.setUploadEnabled(false)
    }

    override fun track(event: Event) {
        event.wrapper?.track(event)
    }

    override fun shouldTrack(event: Event): Boolean {
        return event.wrapper != null
    }
}
