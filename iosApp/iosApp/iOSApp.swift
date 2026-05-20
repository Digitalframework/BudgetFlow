import SwiftUI
import shared

@main
struct iOSApp: App {
    var repository: TransactionRepo {
        MongoDbIos()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView(repository: repository)
        }
    }
}