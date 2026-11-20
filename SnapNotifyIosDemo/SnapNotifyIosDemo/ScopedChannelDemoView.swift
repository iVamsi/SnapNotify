import SwiftUI
import SnapNotify

struct ScopedChannelDemoView: View {
    @State private var sheetOpen = false
    @State private var dialogOpen = false

    var body: some View {
        VStack(spacing: 10) {
            Button("Open scoped bottom sheet") {
                sheetOpen = true
            }
            .buttonStyle(.borderedProminent)
            .frame(maxWidth: .infinity)

            Button("Open scoped dialog") {
                dialogOpen = true
            }
            .buttonStyle(.borderedProminent)
            .frame(maxWidth: .infinity)
        }
        .sheet(isPresented: $sheetOpen) {
            ScopedContentView(channelId: "demo-sheet", title: "Scoped Bottom Sheet")
                .presentationDetents([.medium])
        }
        .fullScreenCover(isPresented: $dialogOpen) {
            ZStack {
                Color.black.opacity(0.45)
                    .ignoresSafeArea()
                    .onTapGesture {
                        dialogOpen = false
                    }

                ScopedContentView(channelId: "demo-dialog", title: "Scoped Dialog") {
                    dialogOpen = false
                }
                .background(Color(.systemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 20))
                .shadow(radius: 16)
                .padding(24)
            }
            .modifier(ClearFullScreenBackground())
        }
    }
}

private struct ClearFullScreenBackground: ViewModifier {
    func body(content: Content) -> some View {
        if #available(iOS 16.4, *) {
            content.presentationBackground(.clear)
        } else {
            content
        }
    }
}

private struct ScopedContentView: View {
    let channelId: String
    let title: String
    var onClose: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: 12) {
            HStack {
                Text(title)
                    .font(.headline)
                Spacer()
                if let onClose {
                    Button("Close") {
                        onClose()
                    }
                    .font(.subheadline)
                }
            }

            Text("Independent channel: \(channelId)")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity, alignment: .leading)

            Button("Scoped success") {
                SnapNotifyIos.shared.showScopedSuccess(
                    channelId: channelId,
                    message: "Saved in \(channelId)"
                )
            }
            .buttonStyle(.borderedProminent)
            .frame(maxWidth: .infinity)

            Button("Scoped Undo") {
                SnapNotifyIos.shared.showScopedUndoable(
                    channelId: channelId,
                    message: "Removed in \(channelId)",
                    durationMillis: 5000,
                    onAction: {},
                    onTimeout: {}
                )
            }
            .buttonStyle(.borderedProminent)
            .frame(maxWidth: .infinity)

            Button("Send to root host") {
                SnapNotifyIos.shared.show(message: "Message in the root host")
            }
            .buttonStyle(.bordered)
            .frame(maxWidth: .infinity)

            SnapNotifyScopedHostRepresentable(channelId: channelId)
                .frame(height: 120)
        }
        .padding(16)
        .onDisappear {
            SnapNotifyIos.shared.clearScopedChannel(channelId: channelId)
        }
    }
}
