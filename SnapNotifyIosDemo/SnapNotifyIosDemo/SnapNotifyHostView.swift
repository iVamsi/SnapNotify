import SwiftUI
import UIKit
import SnapNotify

struct SnapNotifyHostRepresentable: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let controller = SnapNotifyIos.shared.createHostViewController()
        controller.view.backgroundColor = .clear
        controller.view.isOpaque = false
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct SnapNotifyScopedHostRepresentable: UIViewControllerRepresentable {
    let channelId: String

    func makeUIViewController(context: Context) -> UIViewController {
        let controller = SnapNotifyIos.shared.createScopedHostViewController(channelId: channelId)
        controller.view.backgroundColor = .clear
        controller.view.isOpaque = false
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
