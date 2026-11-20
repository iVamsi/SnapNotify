import SwiftUI
import UIKit
import SnapNotifyKmmShared

struct KmmSnapNotifyHost: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let controller = KmmIosBridge.shared.createGlobalHostController()
        controller.view.backgroundColor = .clear
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct KmmCheckoutView: View {
    @State private var itemCount: Int32 = KmmIosBridge.shared.itemCount()
    @State private var subtotal: Int32 = KmmIosBridge.shared.subtotalDollars()
    @State private var ordersSubmitted: Int32 = KmmIosBridge.shared.ordersSubmitted()

    private func refreshState() {
        itemCount = KmmIosBridge.shared.itemCount()
        subtotal = KmmIosBridge.shared.subtotalDollars()
        ordersSubmitted = KmmIosBridge.shared.ordersSubmitted()
    }

    var body: some View {
        VStack(spacing: 0) {
            NavigationStack {
                List {
                    Section("KMM Shared Cart State (commonMain)") {
                        HStack {
                            Text("Items in Cart")
                            Spacer()
                            Text("\(itemCount)").bold()
                        }
                        HStack {
                            Text("Subtotal")
                            Spacer()
                            Text("$\(subtotal)").bold()
                        }
                        HStack {
                            Text("Orders Confirmed")
                            Spacer()
                            Text("\(ordersSubmitted)").bold()
                        }
                    }

                    Section("Shared KMM Presenter Actions") {
                        Button {
                            KmmIosBridge.shared.addHeadphones()
                            refreshState()
                        } label: {
                            Label("Add Headphones ($249)", systemImage: "cart.badge.plus")
                        }

                        Button {
                            KmmIosBridge.shared.removeLastItemWithUndo()
                            refreshState()
                        } label: {
                            Label("Remove Last Item (Undoable)", systemImage: "trash")
                        }

                        Button {
                            KmmIosBridge.shared.submitOrder()
                            refreshState()
                        } label: {
                            Label("Submit Order (Rich Snackbar)", systemImage: "paperplane.fill")
                        }

                        Button {
                            KmmIosBridge.shared.simulateNetworkOutage()
                        } label: {
                            Label("Simulate Urgent Gateway Alert", systemImage: "wifi.exclamationmark")
                        }
                    }
                }
                .navigationTitle("SnapNotify KMM App")
            }

            Divider()

            KmmSnapNotifyHost()
                .frame(height: 150)
        }
        .onAppear {
            KmmIosBridge.shared.submitOrder()
            refreshState()
        }
    }
}

@main
struct SnapNotifyKmmApp: App {
    var body: some Scene {
        WindowGroup {
            KmmCheckoutView()
        }
    }
}
