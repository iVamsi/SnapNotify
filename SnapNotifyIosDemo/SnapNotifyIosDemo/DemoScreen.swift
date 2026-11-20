import SwiftUI
import SnapNotify

struct DemoScreen: View {
    @State private var counter = 0
    @State private var droppedCount = 0
    @State private var deduplicationStrategy: DeduplicationStrategy = .dropduplicate
    @State private var isHapticFeedbackEnabled = true
    @State private var isTopPlacement = SnapNotifyIos.shared.isTopPlacement()
    @State private var didShowInitialToast = false

    private func applyConfiguration(
        maxQueueSize: Int32 = 50,
        strategy: DeduplicationStrategy? = nil,
        hapticsEnabled: Bool? = nil
    ) {
        let resolvedStrategy = strategy ?? deduplicationStrategy
        let resolvedHaptics = hapticsEnabled ?? isHapticFeedbackEnabled
        deduplicationStrategy = resolvedStrategy
        isHapticFeedbackEnabled = resolvedHaptics
        SnapNotifyIos.shared.configure(
            maxQueueSize: maxQueueSize,
            deduplicationStrategy: resolvedStrategy,
            isHapticFeedbackEnabled: resolvedHaptics,
            isAccessibilityScalingEnabled: true
        ) { _ in
            DispatchQueue.main.async {
                droppedCount += 1
            }
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            if isTopPlacement {
                SnapNotifyHostRepresentable()
                    .frame(height: 140)
                Divider()
            }

            NavigationStack {
                ScrollView {
                    VStack(spacing: 12) {
                        Text("Native iOS App (SwiftUI + Xcode)")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                            .padding(.top, 4)

                        ScopedChannelDemoView()

                        SectionHeader("Basic Messages")

                        DemoButton("Show Simple Message") {
                            counter += 1
                            SnapNotifyIos.shared.show(message: "Simple message \(counter)")
                        }

                        DemoButton("Show Long Message") {
                            counter += 1
                            SnapNotifyIos.shared.showLong(message: "Long duration message \(counter)")
                        }

                        DemoButton("Show Message with Action") {
                            counter += 1
                            let current = counter
                            SnapNotifyIos.shared.showWithAction(
                                message: "Message with action \(current)",
                                actionLabel: "Undo"
                            ) {
                                DispatchQueue.main.async {
                                    SnapNotifyIos.shared.show(message: "Undo action performed!")
                                    counter -= 1
                                }
                            }
                        }

                        DemoButton("Test Message Queue") {
                            for index in 1...3 {
                                SnapNotifyIos.shared.show(message: "Queued message \(index)")
                            }
                        }

                        DemoButton("Clear All Messages", role: .destructive) {
                            SnapNotifyIos.shared.clearAll()
                        }

                        SectionHeader("Semantic Styles")

                        DemoButton("Show Success Message") {
                            counter += 1
                            SnapNotifyIos.shared.showSuccess(
                                message: "Operation completed successfully! \(counter)"
                            )
                        }

                        DemoButton("Show Error Message") {
                            counter += 1
                            SnapNotifyIos.shared.showError(
                                message: "Something went wrong! \(counter)"
                            )
                        }

                        DemoButton("Show Warning Message") {
                            counter += 1
                            SnapNotifyIos.shared.showWarning(
                                message: "Please check your input! \(counter)"
                            )
                        }

                        DemoButton("Show Info Message") {
                            counter += 1
                            SnapNotifyIos.shared.showInfo(
                                message: "Here's some information! \(counter)"
                            )
                        }

                        SectionHeader("Custom Styles")

                        DemoButton("Custom Purple Style") {
                            counter += 1
                            SnapNotifyIos.shared.showStyled(
                                message: "Custom purple theme! \(counter)",
                                containerColorArgb: 0xFF6A1B9A,
                                contentColorArgb: 0xFFFFFFFF,
                                actionColorArgb: 0xFFE1BEE7,
                                cornerRadiusDp: 16,
                                elevationDp: 12,
                                actionLabel: nil,
                                onAction: nil
                            )
                        }

                        DemoButton("Custom Style with Action") {
                            SnapNotifyIos.shared.showStyled(
                                message: "Custom styled with action!",
                                containerColorArgb: 0xFF2E7D32,
                                contentColorArgb: 0xFFFFFFFF,
                                actionColorArgb: 0xFF81C784,
                                cornerRadiusDp: 8,
                                elevationDp: 6,
                                actionLabel: "Cool!"
                            ) {
                                DispatchQueue.main.async {
                                    SnapNotifyIos.shared.show(message: "Custom action performed!")
                                }
                            }
                        }

                        DemoButton("Dark Custom Style") {
                            counter += 1
                            SnapNotifyIos.shared.showStyled(
                                message: "Dark theme message! \(counter)",
                                containerColorArgb: 0xFF212121,
                                contentColorArgb: 0xFFE0E0E0,
                                actionColorArgb: 0xFF64B5F6,
                                cornerRadiusDp: 4,
                                elevationDp: 6,
                                actionLabel: nil,
                                onAction: nil
                            )
                        }

                        SectionHeader("Advanced Features")

                        DemoButton("Show Urgent Message (Preempts active)") {
                            SnapNotifyIos.shared.showUrgent(
                                message: "URGENT: Preempted current notification!",
                                actionLabel: "Acknowledge",
                                onAction: {}
                            )
                        }

                        DemoButton("Deduplication: \(deduplicationStrategy.name)") {
                            let strategies = DeduplicationStrategy.entries
                            let index = strategies.firstIndex(of: deduplicationStrategy) ?? 0
                            let next = strategies[(index + 1) % strategies.count]
                            applyConfiguration(strategy: next)
                        }

                        DemoButton("Haptics: \(isHapticFeedbackEnabled ? "On" : "Off")") {
                            applyConfiguration(hapticsEnabled: !isHapticFeedbackEnabled)
                        }

                        DemoButton("Overflow a 3-slot queue (dropped: \(droppedCount))") {
                            applyConfiguration(maxQueueSize: 3)
                            for index in 1...6 {
                                SnapNotifyIos.shared.show(message: "Overflow message \(index)")
                            }
                        }

                        DemoButton("Rich snackbar") {
                            SnapNotifyIos.shared.showRich(
                                title: "File uploaded",
                                message: "Report_Q3.pdf saved to Cloud Drive",
                                actionLabel: "View",
                                showLeadingCheckIcon: true,
                                showCloseButton: true
                            ) {
                                DispatchQueue.main.async {
                                    SnapNotifyIos.shared.show(message: "Opening file")
                                }
                            }
                        }

                        DemoButton("Undo with countdown") {
                            SnapNotifyIos.shared.showUndoable(
                                message: "Item moved to trash",
                                durationMillis: 5000,
                                onAction: {
                                    DispatchQueue.main.async {
                                        SnapNotifyIos.shared.show(message: "Item restored")
                                    }
                                },
                                onTimeout: {
                                    DispatchQueue.main.async {
                                        SnapNotifyIos.shared.show(message: "Item deleted")
                                    }
                                }
                            )
                        }

                        SectionHeader("Notification Placement")

                        DemoButton("Place at bottom") {
                            SnapNotifyIos.shared.setPlacementBottom()
                            isTopPlacement = false
                        }

                        DemoButton("Place as top pill") {
                            SnapNotifyIos.shared.setPlacementTopPill()
                            isTopPlacement = true
                        }

                        DemoButton("Place as top banner") {
                            SnapNotifyIos.shared.setPlacementTopBanner()
                            isTopPlacement = true
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 16)
                }
                .navigationTitle("SnapNotify iOS Demo")
                .navigationBarTitleDisplayMode(.inline)
            }

            if !isTopPlacement {
                Divider()
                SnapNotifyHostRepresentable()
                    .frame(height: 150)
            }
        }
        .onAppear {
            if !didShowInitialToast {
                didShowInitialToast = true
                SnapNotifyIos.shared.showRich(
                    title: "SnapNotify 2.0",
                    message: "Native SwiftUI Xcode Demo ready!",
                    actionLabel: "Action",
                    showLeadingCheckIcon: true,
                    showCloseButton: true,
                    onAction: {}
                )
            }
        }
    }
}

private struct SectionHeader: View {
    let title: String

    init(_ title: String) {
        self.title = title
    }

    var body: some View {
        Text(title)
            .font(.footnote.weight(.semibold))
            .foregroundStyle(.secondary)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.top, 12)
    }
}

private struct DemoButton: View {
    let title: String
    let role: ButtonRole?
    let action: () -> Void

    init(_ title: String, role: ButtonRole? = nil, action: @escaping () -> Void) {
        self.title = title
        self.role = role
        self.action = action
    }

    var body: some View {
        Button(role: role, action: action) {
            Text(title)
                .frame(maxWidth: .infinity)
        }
        .buttonStyle(.borderedProminent)
    }
}
