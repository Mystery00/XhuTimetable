import SwiftUI
import WidgetKit

struct TodayWidgetView: View {
    let entry: TimetableEntry
    @Environment(\.widgetFamily) private var family

    private var day: WidgetDay? { entry.snapshot?.day(at: entry.date) }
    private var items: [WidgetCourseItem] { day?.remainingItems(at: entry.date) ?? [] }
    private var limit: Int { family == .systemSmall ? 1 : (family == .systemMedium ? 3 : 6) }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .center, spacing: 6) {
                Text(family == .systemSmall ? "下一节课" : "今日课程")
                    .font(.system(size: family == .systemSmall ? 14 : 15, weight: .bold))
                Spacer(minLength: 2)
                if let week = day?.week, week > 0 {
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
            } else {
                let shown = family == .systemSmall ? items.filter { $0.kind == "course" } : items
                if shown.isEmpty {
                    WidgetEmptyView(text: day?.items.contains(where: { $0.kind == "course" }) == true ? "今日课程已结束" : "今天没有课程，享受自由时光")
                } else if family == .systemSmall, let item = shown.first {
                    smallCourse(item)
                } else {
                    VStack(spacing: 6) {
                        ForEach(Array(shown.prefix(limit).enumerated()), id: \.offset) { _, item in
                            courseRow(item)
                        }
                    }
                    Spacer(minLength: 0)
                    if shown.count > limit {
                        Text("还有 \(shown.count - limit) 项，打开应用查看")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
    }

    private func smallCourse(_ item: WidgetCourseItem) -> some View {
        let ongoing = item.startDate <= entry.date
        return VStack(alignment: .leading, spacing: 5) {
            HStack(spacing: 5) {
                RoundedRectangle(cornerRadius: 2).fill(item.color).frame(width: 3.5, height: 12)
                Text(ongoing ? "正在上课" : "即将开始")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(item.color)
            }
            Text(item.title)
                .font(.system(size: 15, weight: .bold))
                .lineLimit(2)
                .minimumScaleFactor(0.85)
            if !item.location.isEmpty {
                Text(item.location)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }
            Spacer(minLength: 0)
            Text(item.timeText)
                .font(.system(size: 11, weight: .medium))
                .lineLimit(1)
                .minimumScaleFactor(0.85)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(.secondarySystemBackground))
                .cornerRadius(8)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
    }

    private func courseRow(_ item: WidgetCourseItem) -> some View {
        let ongoing = item.kind == "course" && item.startDate <= entry.date
        return HStack(spacing: 8) {
            RoundedRectangle(cornerRadius: 2)
                .fill(item.color)
                .frame(width: 3.5, height: 28)
            VStack(alignment: .leading, spacing: 2) {
                Text(item.title)
                    .font(.system(size: 13, weight: .semibold))
                    .lineLimit(1)
                let sub = item.location.isEmpty ? (item.kind == "thing" ? "自定义事项" : "课程") : item.location
                Text(ongoing ? "正在上课 · \(sub)" : sub)
                    .font(.system(size: 11))
                    .foregroundColor(ongoing ? item.color : .secondary)
                    .lineLimit(1)
            }
            Spacer(minLength: 4)
            Text(item.timeText)
                .font(.system(size: 11, weight: item.kind == "thing" || ongoing ? .semibold : .regular))
                .foregroundColor(item.kind == "thing" ? item.color : .secondary)
                .multilineTextAlignment(.trailing)
                .lineLimit(2)
        }
        .padding(.horizontal, 9)
        .padding(.vertical, 6)
        .background(Color(.secondarySystemBackground).opacity(0.75))
        .cornerRadius(10)
    }
}
