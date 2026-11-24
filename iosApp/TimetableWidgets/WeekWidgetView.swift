import SwiftUI

struct WeekWidgetView: View {
    let entry: TimetableEntry
    private let weekdays = ["一", "二", "三", "四", "五", "六", "日"]

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .center) {
                Text("本周课表")
                    .font(.system(size: 15, weight: .bold))
                Spacer()
                if let week = entry.snapshot?.day(at: entry.date)?.week, week > 0 {
                    Text("第\(week)周")
                        .font(.system(size: 10, weight: .semibold))
                        .foregroundColor(.accentColor)
                        .padding(.horizontal, 7)
                        .padding(.vertical, 2)
                        .background(Color.accentColor.opacity(0.14))
                        .clipShape(Capsule())
                }
            }
            if let message = entry.snapshot?.message(at: entry.date) ?? (entry.snapshot == nil ? "打开西瓜课表同步课程" : nil) {
                WidgetEmptyView(text: message)
            } else if let snapshot = entry.snapshot {
                let days = snapshot.week(at: entry.date)
                if days.allSatisfy({ $0.blocks.isEmpty }) {
                    WidgetEmptyView(text: "本周暂无课程")
                } else {
                    grid(days)
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
    }

    private func grid(_ days: [WidgetDay]) -> some View {
        GeometryReader { geometry in
            let header: CGFloat = 29
            let rowHeight = max(1, (geometry.size.height - header) / 11)
            let todayKey = WidgetSnapshot.dateKey(entry.date)
            HStack(alignment: .top, spacing: 2) {
                VStack(spacing: 0) {
                    Color.clear.frame(height: header)
                    ForEach(1...11, id: \.self) { period in
                        Text("\(period)")
                            .font(.system(size: 9, weight: .medium))
                            .foregroundColor(.secondary)
                            .frame(height: rowHeight)
                    }
                }.frame(width: 13)
                ForEach(Array(days.enumerated()), id: \.offset) { index, day in
                    let isToday = day.date == todayKey
                    VStack(spacing: 0) {
                        VStack(spacing: 1) {
                            Text(weekdays[index])
                                .font(.system(size: 9, weight: isToday ? .bold : .medium))
                                .foregroundColor(isToday ? .accentColor : .secondary)
                            Text(String(day.date.suffix(2)))
                                .font(.system(size: 9, weight: isToday ? .bold : .medium))
                                .foregroundColor(isToday ? .white : .primary)
                                .padding(.horizontal, 4)
                                .padding(.vertical, 1)
                                .background(isToday ? Color.accentColor : Color.clear)
                                .clipShape(Capsule())
                        }
                        .frame(height: header)
                        GeometryReader { column in
                            ZStack(alignment: .topLeading) {
                                ForEach(0..<11, id: \.self) { row in
                                    Rectangle()
                                        .fill(Color.secondary.opacity(0.08))
                                        .frame(height: 0.5)
                                        .offset(y: CGFloat(row) * rowHeight)
                                }
                                ForEach(Array(day.blocks.enumerated()), id: \.offset) { _, block in
                                    VStack(alignment: .leading, spacing: 1) {
                                        Text(block.item.title)
                                            .font(.system(size: 9, weight: .semibold))
                                            .lineLimit(3)
                                        if !block.item.location.isEmpty {
                                            Text(block.item.location)
                                                .font(.system(size: 8))
                                                .foregroundColor(.secondary)
                                                .lineLimit(2)
                                        }
                                        if block.count > 1 {
                                            Text("+\(block.count - 1)")
                                                .font(.system(size: 8, weight: .bold))
                                                .foregroundColor(block.item.color)
                                        }
                                    }
                                    .padding(2.5)
                                    .frame(
                                        width: column.size.width,
                                        height: max(1, CGFloat(block.end - block.start + 1) * rowHeight - 2),
                                        alignment: .topLeading
                                    )
                                    .background(block.item.color.opacity(0.20))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 6)
                                            .stroke(block.item.color.opacity(0.35), lineWidth: 0.5)
                                    )
                                    .cornerRadius(6)
                                    .clipped()
                                    .offset(y: CGFloat(block.start - 1) * rowHeight + 1)
                                }
                            }
                        }
                    }.frame(maxWidth: .infinity)
                }
            }
        }
    }
}
