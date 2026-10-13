# Pet Chat

A small Telegram-style messenger for Android, built as a code sample. It has two screens, a chat list and a conversation, on top of a hand-written Redux/MVI core.

There is no backend. An in-memory mock repository simulates latency, random failures, delivery and read receipts, replies and incoming messages, so every UI state can be demoed offline.

**Stack:** Kotlin · Jetpack Compose (Material 3) · Coroutines and Flow · Hilt · Navigation Compose (type-safe routes) · multi-module.

## What it shows

| Area | Implementation |
| --- | --- |
| Architecture | Redux-style MVI: one immutable `State`, `Action`s, a pure `Reducer`, a classic `Middleware` chain, one-off `Effect`s |
| Layers | UI → ViewModel (owns the `Store`) → middleware → use cases → repository interfaces (`:domain`) → mock implementation (`:data`) |
| Pagination | Keyset (cursor) pagination, duplicate-request guard, automatic retry on reconnect |
| Optimistic send | A message appears instantly as `PENDING`, then `SENT → DELIVERED → READ`, or `FAILED` (tap to retry) |
| Connectivity | `ConnectivityManager` callback → `Waiting for network… / Connecting…` in the title bar, like Telegram; offline messages go out in order on reconnect |
| Compose performance | Stable models, stable keys and content types, `derivedStateOf`, no work during composition |
| Tests | Store, reducers, pagination middleware, use cases: plain JVM tests with fakes, no mocking library |

## Modules

```
:app                    MainActivity, navigation, Hilt setup
:core:mvi               Store, Reducer, Middleware (pure Kotlin)
:core:ui                Theme, shared components, time formatting
:domain                 Models, repository interfaces, use cases (pure Kotlin)
:data                   Mock repository, RemoteMessage -> Message mapper, connectivity
:feature:chatlist       Chat list screen
:feature:conversation   Conversation screen
```

Features depend only on `:domain`, `:core:mvi` and `:core:ui`, never on each other or on `:data`. `:app` is the only module that sees everything; it wires navigation and the Hilt graph.

## Redux core (`:core:mvi`)

```
UI ──Action──▶ Middleware chain ──▶ Reducer ──▶ StateFlow<State> ──▶ UI
                   │      ▲
               use cases  └── result Actions
```

- **`Store`** processes actions one at a time from a channel. The reducer never runs concurrently, so updates from paging, incoming messages and user input cannot overwrite each other.
- **`Reducer`** is a pure function, `(State, Action) -> State`.
- **`Middleware`** has the classic `store → next → action` shape. It can pass an action on, swallow it (the pagination guard does this) or start side effects and report the results back as new actions.
- **`context.launch(key)`** cancels a previous job with the same key ("latest wins") without manual `Job` bookkeeping.
- **`LoggingMiddleware`** logs every action in debug builds (filter Logcat by `MVI/`).

The ViewModels only own the store's lifecycle. All logic lives in reducers and middleware, which are plain classes and easy to test.

## Conversation screen flow

1. `Init`: `PaginationMiddleware` loads the first page; `ConversationObserverMiddleware` starts observing the header, the messages and the connection.
2. Scrolling near the top dispatches `LoadOlder`. If a page is already loading or the history is complete, the middleware drops the action before it reaches the reducer.
3. The repository loads the page and adds it to its message flow. The observer middleware maps it to UI items on `Dispatchers.Default`, and `MessagesUpdated` puts them in the state.
4. `Send` adds a `PENDING` message to the same flow, so it shows up immediately. Sending runs in the data layer's scope, so leaving the screen does not cancel it.

## Compose performance

- All UI state classes are `@Immutable` and use `ImmutableList`, so composables skip when their inputs did not change.
- Lazy lists use stable keys (`localId`, which never changes after send) and `contentType`, so item compositions are reused.
- Mapping, sorting and date formatting run in middleware on `Dispatchers.Default`; composables only read ready-made strings.
- `derivedStateOf` for the jump-to-bottom button and the send button: they recompose when a boolean flips, not on every scroll pixel or keystroke.
- Scroll-based paging uses `snapshotFlow` outside composition.
- The message draft uses `TextFieldState` inside the UI, not the store, which avoids a store round-trip on every keystroke.
- `collectAsStateWithLifecycle` stops collection in the background.

## Running

Open in Android Studio, sync Gradle, run `app`. Min SDK 26.

```bash
./gradlew test   # unit tests
```

### Things to try

- Turn on airplane mode: the title switches to "Waiting for network…". Send a few messages; they stay `PENDING` (clock icon). Turn the network back on: "Connecting…", then the messages go out in order.
- Scroll up in a long chat to load older pages. About one request in ten fails on purpose; tap the retry row, or go offline and back online to see the automatic retry.
- Send a message and watch the ticks: sent, delivered, read, then a reply arrives.
- Stay on the chat list: incoming messages reorder chats and update unread badges.

## Next steps

- Replace the mock with a real API: Retrofit or Ktor for history, a WebSocket for real-time events. The domain interfaces stay the same.
- Offline-first cache with Room as the single source of truth, plus a WorkManager outbox for guaranteed delivery after process death.
- Push notifications (FCM, `MessagingStyle`), screenshot tests, a baseline profile module.
