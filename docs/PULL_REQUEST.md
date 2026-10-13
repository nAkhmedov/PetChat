# Conversation screen: paginated history with optimistic send

## Summary

Adds the conversation screen on top of the Redux core. Opening a chat shows the newest messages, loads older pages as the user scrolls up, and sends messages optimistically.

## Changes

- `ConversationState`, actions and effects; `ConversationReducer` (pure).
- `PaginationMiddleware`: duplicate-request guard, page loading, automatic retry when the connection comes back after a failure.
- `ConversationObserverMiddleware`: observes the header, the messages and the connection; mapping runs on `Dispatchers.Default`.
- `SendMessageMiddleware`: optimistic send and retry; scrolls to the bottom after sending.
- `MockChatRepository.loadOlder`: keyset pagination (messages older than the oldest one loaded).
- UI: reverse-layout `LazyColumn` with stable keys and content types, date separators, grouped bubbles, a jump-to-bottom button, and a `TextFieldState` input.

## Design decisions

- **The guard lives in middleware, not in the reducer.** A duplicate `LoadOlder` never reaches the reducer, so the reducer only records what happened and stays trivial to test.
- **The draft is not in the store.** Routing each keystroke through an asynchronous store causes cursor jumps and full state copies. Only the final text is dispatched.
- **One message flow for everything.** Loaded pages, sent messages, incoming messages and status changes all arrive through the same repository flow, so nothing is patched into the state by hand.

## Testing

- `PaginationMiddlewareTest`: first page, duplicate requests, next page, end of history, failure and automatic retry. It uses the real store and reducer with a fake repository.
- `ConversationReducerTest`: state transitions.
- Manual: turn airplane mode on and off while sending, scroll to the top of a long chat, rotate the device during a page load.

## Screenshots

| Chat list | Conversation | Offline |
| --- | --- | --- |
| _add_ | _add_ | _add_ |
