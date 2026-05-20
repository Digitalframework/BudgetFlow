import SwiftUI
import shared

struct ContentView: View {
    @StateObject private var viewModel: TransactionViewModel
    @State private var showingSidebar = false
    @State private var selectedFile: URL?
    
    init(repository: TransactionRepo) {
        _viewModel = StateObject(wrappedValue: TransactionViewModel(repository: repository))
    }
    
    var body: some View {
        NavigationView {
            ZStack {
                VStack(spacing: 16) {
                    // Upload Panel
                    UploadPanel(selectedFile: $selectedFile)
                    
                    // Summary Cards
                    SummaryCards(transactions: viewModel.uiState.transactions)
                    
                    // Category Breakdown
                    CategoryBreakdown(transactions: viewModel.uiState.transactions)
                    
                    // Transaction List
                    TransactionList(
                        transactions: viewModel.uiState.transactions,
                        onCategoryChange: viewModel.updateCategory
                    )
                }
                .padding()
                
                // Sidebar Overlay
                if showingSidebar {
                    SidebarFilters(
                        currentFilter: viewModel.uiState.filter,
                        onFilterChange: viewModel.setFilter,
                        onClose: { showingSidebar = false }
                    )
                    .transition(.move(edge: .leading))
                }
            }
            .navigationTitle("Banking App")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { showingSidebar = true }) {
                        Image(systemName: "line.3.horizontal")
                    }
                }
            }
        }
    }
}

struct UploadPanel: View {
    @Binding var selectedFile: URL?
    @State private var showingFilePicker = false
    
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "cloud.fill")
                .font(.system(size: 40))
                .foregroundColor(.blue)
            
            Text("Upload Bank Statement PDF")
                .font(.title3)
                .fontWeight(.medium)
            
            Text(selectedFile?.lastPathComponent ?? "Tap to select PDF file")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 30)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.blue.opacity(0.3), lineWidth: 2)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(Color.blue.opacity(0.05))
                )
        )
        .onTapGesture {
            showingFilePicker = true
        }
        .fileImporter(
            isPresented: $showingFilePicker,
            allowedContentTypes: [.pdf],
            allowsMultipleSelection: false
        ) { result in
            switch result {
            case .success(let urls):
                if let url = urls.first {
                    selectedFile = url
                }
            case .failure(let error):
                print("File picker error: \(error)")
            }
        }
    }
}

struct SummaryCards: View {
    let transactions: [Transaction]
    
    var totalAmount: Double {
        transactions.reduce(0) { $0 + $1.amount }
    }
    
    var topCategory: (name: String, amount: Double)? {
        let grouped = Dictionary(grouping: transactions, by: { $0.category })
        let summed = grouped.mapValues { $0.reduce(0) { $0 + $1.amount } }
        return summed.max(by: { $0.value < $1.value })
            .map { (name: $0.key, amount: $0.value) }
    }
    
    var body: some View {
        HStack(spacing: 12) {
            // Total Card
            SummaryCard(
                title: "Total",
                value: String(format: "€%.2f", totalAmount),
                icon: "eurosign.circle.fill",
                color: .blue
            )
            
            // Count Card
            SummaryCard(
                title: "Transactions",
                value: "\(transactions.count)",
                icon: "list.bullet",
                color: .green
            )
            
            // Top Category Card
            SummaryCard(
                title: "Top Category",
                value: topCategory != nil ? String(format: "€%.2f", topCategory!.amount) : "€0.00",
                icon: "chart.pie.fill",
                color: .orange
            )
        }
    }
}

struct SummaryCard: View {
    let title: String
    let value: String
    let icon: String
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: icon)
                    .foregroundColor(color)
                Text(title)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            Text(value)
                .font(.title2)
                .fontWeight(.bold)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(color.opacity(0.1))
        )
    }
}

struct CategoryBreakdown: View {
    let transactions: [Transaction]
    
    var breakdown: [(category: String, percentage: Double, amount: Double)] {
        let total = transactions.reduce(0) { $0 + $1.amount }
        guard total > 0 else { return [] }
        
        let grouped = Dictionary(grouping: transactions, by: { $0.category })
        let summed = grouped.mapValues { $0.reduce(0) { $0 + $1.amount } }
        
        return summed.map { (category: $0.key, percentage: $0.value / total * 100, amount: $0.value) }
            .sorted { $0.percentage > $1.percentage }
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Category Breakdown")
                .font(.title2)
                .fontWeight(.bold)
            
            if breakdown.isEmpty {
                Text("No data to display")
                    .foregroundColor(.secondary)
            } else {
                ForEach(breakdown, id: \.category) { item in
                    HStack {
                        let category = CategoryMapper.shared.getCategory(name: item.category)
                        Text(category?.icon ?? "❓")
                        Text(category?.label ?? item.category)
                        Spacer()
                        Text(String(format: "%.1f%%", item.percentage))
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.systemBackground))
                .shadow(color: .black.opacity(0.05), radius: 5)
        )
    }
}

struct TransactionList: View {
    let transactions: [Transaction]
    let onCategoryChange: (String, String) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Transactions")
                .font(.title2)
                .fontWeight(.bold)
            
            if transactions.isEmpty {
                Text("No transactions found. Upload a PDF to get started.")
                    .foregroundColor(.secondary)
            } else {
                ForEach(transactions, id: \.id) { transaction in
                    TransactionRow(
                        transaction: transaction,
                        onCategoryChange: onCategoryChange
                    )
                }
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(.systemBackground))
                .shadow(color: .black.opacity(0.05), radius: 5)
        )
    }
}

struct TransactionRow: View {
    let transaction: Transaction
    let onCategoryChange: (String, String) -> Void
    @State private var showingCategoryPicker = false
    
    var category: Category? {
        CategoryMapper.shared.getCategory(name: transaction.category)
    }
    
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(transaction.date)
                    .font(.caption)
                    .foregroundColor(.secondary)
                Text(transaction.description)
                    .font(.body)
                    .lineLimit(1)
            }
            
            Spacer()
            
            Text(String(format: "-€%.2f", transaction.amount))
                .foregroundColor(.red)
                .fontWeight(.medium)
            
            Menu {
                ForEach(CategoryMapper.shared.allCategories(), id: \.name) { cat in
                    Button {
                        onCategoryChange(transaction.id, cat.name)
                    } label: {
                        HStack {
                            Text(cat.icon)
                            Text(cat.label)
                        }
                    }
                }
            } label: {
                HStack {
                    Text(category?.icon ?? "❓")
                    Text(category?.label ?? transaction.category)
                }
                .font(.caption)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color(hex: category?.color ?? "#8c8c8c").opacity(0.2))
                )
            }
        }
        .padding(.vertical, 4)
    }
}

struct SidebarFilters: View {
    let currentFilter: TransactionFilter
    let onFilterChange: (TransactionFilter) -> Void
    let onClose: () -> Void
    
    @State private var searchQuery: String = ""
    
    var body: some View {
        ZStack(alignment: .leading) {
            Color.black.opacity(0.3)
                .ignoresSafeArea()
                .onTapGesture { onClose() }
            
            VStack(spacing: 0) {
                HStack {
                    Text("Filter")
                        .font(.title2)
                        .fontWeight(.bold)
                    Spacer()
                    Button(action: onClose) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title2)
                    }
                }
                .padding()
                
                Divider()
                
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        // Search
                        TextField("Search...", text: $searchQuery)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .onChange(of: searchQuery) { newValue in
                                onFilterChange(TransactionFilter(category: currentFilter.category, month: currentFilter.month, search: newValue))
                            }
                        
                        Text("Category")
                            .font(.headline)
                        
                        // Category chips
                        FlowLayout {
                            FilterChip(
                                title: "All",
                                isSelected: currentFilter.category == "all"
                            ) {
                                onFilterChange(TransactionFilter(category: "all", month: currentFilter.month, search: currentFilter.search))
                            }
                            
                            ForEach(CategoryMapper.shared.allCategories(), id: \.name) { cat in
                                FilterChip(
                                    title: "\(cat.icon) \(cat.label)",
                                    isSelected: currentFilter.category == cat.name
                                ) {
                                    onFilterChange(TransactionFilter(category: cat.name, month: currentFilter.month, search: currentFilter.search))
                                }
                            }
                        }
                    }
                    .padding()
                }
            }
            .frame(width: 280)
            .background(Color(.systemBackground))
            .shadow(radius: 10)
        }
    }
}

struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.caption)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(isSelected ? Color.blue : Color.gray.opacity(0.2))
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(16)
        }
    }
}

// Simple FlowLayout implementation
struct FlowLayout: Layout {
    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = FlowResult(in: proposal.width ?? 0, subviews: subviews)
        return result.size
    }
    
    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = FlowResult(in: bounds.width, subviews: subviews)
        for (index, subview) in subviews.enumerated() {
            subview.place(at: CGPoint(x: bounds.minX + result.positions[index].x, y: bounds.minY + result.positions[index].y), proposal: .unspecified)
        }
    }
    
    struct FlowResult {
        var positions: [CGPoint] = []
        var size: CGSize = .zero
        
        init(in maxWidth: CGFloat, subviews: Subviews) {
            var x: CGFloat = 0
            var y: CGFloat = 0
            var rowHeight: CGFloat = 0
            
            for subview in subviews {
                let size = subview.sizeThatFits(.unspecified)
                if x + size.width > maxWidth && x > 0 {
                    x = 0
                    y += rowHeight + 8
                    rowHeight = 0
                }
                positions.append(CGPoint(x: x, y: y))
                rowHeight = max(rowHeight, size.height)
                x += size.width + 8
            }
            self.size = CGSize(width: maxWidth, height: y + rowHeight)
        }
    }
}

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3:
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6:
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8:
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}