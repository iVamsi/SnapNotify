import SwiftUI
import UIKit
import SnapNotifyIosBridge

struct SnapNotifyHostRepresentable: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let controller = SnapNotifyIosBridge.shared.createHostViewController()
        controller.view.backgroundColor = .clear
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @State private var tapCount = 0

    var body: some View {
        VStack(spacing: 0) {
            NavigationStack {
                List {
                    Section("SwiftUI Native Controls -> SnapNotify") {
                        Button {
                            tapCount += 1
                            SnapNotifyIosBridge.shared.showSuccess(
                                message: "Saved from native SwiftUI (#\(tapCount))"
                            )
                        } label: {
                            Label("Show Success Snackbar", systemImage: "checkmark.circle.fill")
                        }

                        Button {
                            tapCount += 1
                            SnapNotifyIosBridge.shared.showError(
                                message: "Sync failed in iOS native layer (#\(tapCount))"
                            )
                        } label: {
                            Label("Show Error Snackbar", systemImage: "xmark.octagon.fill")
                        }

                        Button {
                            SnapNotifyIosBridge.shared.showWarning(
                                message: "Low storage space on iPhone"
                            )
                        } label: {
                            Label("Show Warning Snackbar", systemImage: "exclamationmark.triangle.fill")
                        }

                        Button {
                            SnapNotifyIosBridge.shared.showRich(
                                title: "AirDrop Complete",
                                message: "Design_Spec_v2.fig saved to Files"
                            )
                        } label: {
                            Label("Show Rich Snackbar (Title + Close)", systemImage: "doc.badge.plus")
                        }

                        Button {
                            SnapNotifyIosBridge.shared.showUndoable(
                                message: "Photo moved to Recently Deleted"
                            )
                        } label: {
                            Label("Show Undoable Countdown (5s)", systemImage: "arrow.uturn.backward.circle")
                        }

                        Button {
                            SnapNotifyIosBridge.shared.showUrgent(
                                message: "URGENT: Security key verification required"
                            )
                        } label: {
                            Label("Trigger Urgent Preemption", systemImage: "bolt.shield.fill")
                        }
                    }

                    Section("Placement & Queue") {
                        Button("Bottom Snackbar Placement") {
                            SnapNotifyIosBridge.shared.useBottomPlacement()
                        }
                        Button("Top Pill Placement") {
                            SnapNotifyIosBridge.shared.useTopPillPlacement()
                        }
                        Button("Clear All Queued Messages", role: .destructive) {
                            SnapNotifyIosBridge.shared.clearAll()
                        }
                    }
                }
                .navigationTitle("SnapNotify iOS Native")
            }

            Divider()

            SnapNotifyHostRepresentable()
                .frame(height: 150)
        }
        .onAppear {
            SnapNotifyIosBridge.shared.showSuccess(
                message: "SwiftUI + SnapNotify 2.0 ready!"
            )
        }
    }
}

@main
struct SnapNotifyIosNativeApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
