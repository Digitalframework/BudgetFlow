@file:JsModule("antd")
@file:JsNonModule

package antd

import react.FC
import react.Props
import web.html.HTMLInputElement
import web.html.HTMLElement
import web.file.File
import react.dom.events.ChangeEventHandler
import react.dom.events.KeyboardEventHandler
import react.dom.events.MouseEventHandler

// ==================== Layout ====================

external interface LayoutProps : Props {
    var style: dynamic
    var className: String?
    var children: dynamic
}

external interface LayoutComponent : FC<LayoutProps> {
    val Sider: FC<SiderProps>
    val Header: FC<HeaderProps>
    val Content: FC<ContentProps>
    val Footer: FC<FooterProps>
}

external val Layout: LayoutComponent

external interface SiderProps : Props {
    var theme: String?
    var width: dynamic
    var collapsed: Boolean?
    var collapsible: Boolean?
    var collapsedWidth: dynamic
    var onCollapse: ((collapsed: Boolean, type: String) -> Unit)?
    var trigger: dynamic
    var breakpoint: String?
    var style: dynamic
    var className: String?
    var children: dynamic
    var onClick: MouseEventHandler<HTMLElement>?
}

// ==================== Grid ====================

external interface RowProps : Props {
    var gutter: dynamic
    var align: String?
    var justify: String?
    var wrap: Boolean?
    var style: dynamic
    var className: String?
    var children: dynamic
}

external val Row: FC<RowProps>

external interface ColProps : Props {
    var span: dynamic
    var offset: dynamic
    var flex: dynamic
    var xs: dynamic
    var sm: dynamic
    var md: dynamic
    var lg: dynamic
    var xl: dynamic
    var xxl: dynamic
    var style: dynamic
    var className: String?
    var children: dynamic
}

external val Col: FC<ColProps>

// ==================== ConfigProvider / theme ====================

external interface ConfigProviderProps : Props {
    var theme: dynamic
    var locale: dynamic
    var componentSize: String?
    var children: dynamic
}

external val ConfigProvider: FC<ConfigProviderProps>

external interface ThemeObject {
    val defaultAlgorithm: dynamic
    val darkAlgorithm: dynamic
    val compactAlgorithm: dynamic
}

external val theme: ThemeObject

external interface HeaderProps : Props {
    var style: dynamic
    var className: String?
    var children: dynamic
}

external interface ContentProps : Props {
    var style: dynamic
    var className: String?
    var children: dynamic
}

external interface FooterProps : Props {
    var style: dynamic
    var className: String?
    var children: dynamic
}

// ==================== Button ====================

external interface ButtonProps : Props {
    var type: String?
    var danger: Boolean?
    var disabled: Boolean?
    var size: String?
    var loading: Boolean?
    var onClick: MouseEventHandler<HTMLElement>?
    var children: dynamic
    var style: dynamic
    var className: String?
    var icon: dynamic
    var shape: String?
    var ghost: Boolean?
    var block: Boolean?
    var htmlType: String?
}

external val Button: FC<ButtonProps>

// ==================== Card ====================

external interface CardProps : Props {
    var style: dynamic
    var className: String?
    var children: dynamic
    var title: dynamic
    var bordered: Boolean?
    var size: String?
    var extra: dynamic
    var hoverable: Boolean?
    var loading: Boolean?
    var cover: dynamic
    var actions: dynamic
}

external val Card: FC<CardProps>

// ==================== Table ====================

external interface TableProps : Props {
    var columns: dynamic
    var dataSource: dynamic
    var pagination: dynamic
    var rowKey: dynamic
    var onRow: ((record: dynamic, index: Number?) -> dynamic)?
    var style: dynamic
    var className: String?
    var size: String?
    var bordered: Boolean?
    var loading: Boolean?
    var scroll: dynamic
    var rowClassName: dynamic
    var expandable: dynamic
    var summary: dynamic
    var locale: dynamic
}

external interface TableSummaryProps : Props {
    var fixed: dynamic
    var children: dynamic
}

external interface TableSummaryRowProps : Props {
    var children: dynamic
    var style: dynamic
    var className: String?
}

external interface TableSummaryCellProps : Props {
    var index: Number
    var colSpan: Number?
    var rowSpan: Number?
    var align: String?
    var children: dynamic
    var style: dynamic
    var className: String?
}

external interface TableSummaryComponent : FC<TableSummaryProps> {
    val Row: FC<TableSummaryRowProps>
    val Cell: FC<TableSummaryCellProps>
}

external interface TableComponent : FC<TableProps> {
    val Summary: TableSummaryComponent
}

external val Table: TableComponent

// ==================== Input ====================

external interface InputProps : Props {
    var placeholder: String?
    var value: String?
    var defaultValue: String?
    var onChange: ChangeEventHandler<HTMLInputElement>?
    var onPressEnter: KeyboardEventHandler<HTMLInputElement>?
    var onBlur: dynamic
    var onFocus: dynamic
    var style: dynamic
    var className: String?
    var size: String?
    var prefix: dynamic
    var suffix: dynamic
    var addonBefore: dynamic
    var addonAfter: dynamic
    var allowClear: Boolean?
    var disabled: Boolean?
    var maxLength: Number?
    var showCount: Boolean?
    var type: String?
}

external interface InputComponent : FC<InputProps> {
    val Search: FC<SearchProps>
}

external val Input: InputComponent

external interface SearchProps : Props {
    var placeholder: String?
    var value: String?
    var defaultValue: String?
    var onChange: ChangeEventHandler<HTMLInputElement>?
    var onSearch: ((String) -> Unit)?
    var style: dynamic
    var className: String?
    var size: String?
    var loading: Boolean?
    var allowClear: Boolean?
    var disabled: Boolean?
    var enterButton: dynamic
}

external val Search: FC<SearchProps>

// ==================== Table Column ====================

external interface TableColumnProps {
    var title: dynamic
    var dataIndex: String?
    var key: String?
    var width: dynamic
    var align: String?
    var render: dynamic
    var ellipsis: Boolean?
    var fixed: String?
    var sorter: dynamic
    var sortOrder: dynamic
    var defaultSortOrder: String?
    var sortDirections: dynamic
    var filters: dynamic
    var onFilter: dynamic
    var filteredValue: dynamic
    var filtered: Boolean?
    var defaultFilteredValue: dynamic
    var className: String?
    var style: dynamic
}

// ==================== Select ====================

external interface SelectOption {
    var label: dynamic
    var value: dynamic
    var disabled: Boolean?
}

external interface SelectProps : Props {
    var value: dynamic
    var defaultValue: dynamic
    var onChange: ((dynamic) -> Unit)?
    var options: dynamic
    var placeholder: String?
    var style: dynamic
    var className: String?
    var size: String?
    var allowClear: Boolean?
    var mode: String?
    var showSearch: Boolean?
    var filterOption: dynamic
    var disabled: Boolean?
    var loading: Boolean?
    var notFoundContent: dynamic
    var dropdownStyle: dynamic
    var popupMatchSelectWidth: Boolean?
    var defaultOpen: Boolean?
    var autoFocus: Boolean?
    var onBlur: dynamic
    var variant: String?
}

external val Select: FC<SelectProps>

// ==================== Upload ====================

external interface UploadProps : Props {
    var name: String?
    var multiple: Boolean?
    var accept: String?
    var showUploadList: dynamic
    var beforeUpload: ((File, dynamic) -> dynamic)?
    var onChange: ((dynamic) -> Unit)?
    var onRemove: ((dynamic) -> dynamic)?
    var fileList: dynamic
    var children: dynamic
    var style: dynamic
    var className: String?
    var disabled: Boolean?
    var action: String?
    var method: String?
    var listType: String?
    var maxCount: Number?
    var customRequest: dynamic
}

external interface UploadComponent : FC<UploadProps> {
    val Dragger: FC<UploadProps>
}

external val Upload: UploadComponent

// ==================== Modal ====================

external interface ModalProps : Props {
    var title: dynamic
    var open: Boolean?
    var onOk: (() -> Unit)?
    var onCancel: (() -> Unit)?
    var okText: dynamic
    var cancelText: dynamic
    var okButtonProps: dynamic
    var cancelButtonProps: dynamic
    var children: dynamic
    var width: dynamic
    var footer: dynamic
    var closable: Boolean?
    var maskClosable: Boolean?
    var destroyOnClose: Boolean?
    var centered: Boolean?
    var style: dynamic
    var className: String?
    var zIndex: Number?
    var confirmLoading: Boolean?
}

external val Modal: FC<ModalProps>

// ==================== Empty ====================

external interface EmptyProps : Props {
    var description: dynamic
    var image: dynamic
    var style: dynamic
    var className: String?
    var children: dynamic
    var imageStyle: dynamic
}

external val Empty: FC<EmptyProps>

// ==================== Spin ====================

external interface SpinProps : Props {
    var spinning: Boolean?
    var tip: String
    var size: String?
    var style: dynamic
    var className: String?
    var children: dynamic
    var delay: Number?
    var indicator: dynamic
    var percent: Number?
}

external val Spin: FC<SpinProps>

// ==================== Tag ====================

external interface TagProps : Props {
    var color: String?
    var style: dynamic
    var className: String?
    var children: dynamic
    var bordered: Boolean?
    var closable: Boolean?
    var onClose: ((dynamic) -> Unit)?
    var icon: dynamic
}

external val Tag: FC<TagProps>

// ==================== Segmented ====================

external interface SegmentedProps : Props {
    var options: dynamic
    var value: dynamic
    var defaultValue: dynamic
    var onChange: ((dynamic) -> Unit)?
    var style: dynamic
    var className: String?
    var size: String?
    var block: Boolean?
    var disabled: Boolean?
}

external val Segmented: FC<SegmentedProps>

// ==================== Progress ====================

external interface ProgressProps : Props {
    var percent: Number?
    var status: String?
    var strokeColor: dynamic
    var trailColor: String?
    var size: dynamic
    var showInfo: Boolean?
    var format: ((Number?) -> dynamic)?
    var style: dynamic
    var className: String?
    var type: String?
    var steps: Number?
    var success: dynamic
}

external val Progress: FC<ProgressProps>

// ==================== Statistic ====================

external interface StatisticProps : Props {
    var title: dynamic
    var value: dynamic
    var prefix: dynamic
    var suffix: dynamic
    var precision: Number?
    var valueStyle: dynamic
    var style: dynamic
    var className: String?
    var formatter: dynamic
    var loading: Boolean?
    var decimalSeparator: String?
    var groupSeparator: String?
}

external val Statistic: FC<StatisticProps>

// ==================== Tooltip ====================

external interface TooltipProps : Props {
    var title: dynamic
    var placement: String?
    var color: String?
    var open: Boolean?
    var children: dynamic
    var style: dynamic
    var className: String?
    var overlayStyle: dynamic
    var trigger: dynamic
    var mouseEnterDelay: Number?
    var arrow: dynamic
}

external val Tooltip: FC<TooltipProps>

// ==================== Dropdown ====================

external interface DropdownProps : Props {
    var menu: dynamic
    var placement: String?
    var trigger: dynamic
    var open: Boolean?
    var onOpenChange: ((Boolean) -> Unit)?
    var children: dynamic
    var disabled: Boolean?
    var overlayStyle: dynamic
}

external val Dropdown: FC<DropdownProps>

// ==================== Divider ====================

external interface DividerProps : Props {
    var type: String?
    var orientation: String?
    var plain: Boolean?
    var dashed: Boolean?
    var style: dynamic
    var className: String?
    var children: dynamic
}

external val Divider: FC<DividerProps>

// ==================== Space ====================

external interface SpaceProps : Props {
    var size: dynamic
    var direction: String?
    var align: String?
    var wrap: Boolean?
    var style: dynamic
    var className: String?
    var children: dynamic
    var split: dynamic
}

external val Space: FC<SpaceProps>

// ==================== Typography ====================

external interface TypographyTextProps : Props {
    var strong: Boolean?
    var italic: Boolean?
    var underline: Boolean?
    var delete: Boolean?
    var code: Boolean?
    var mark: Boolean?
    var type: String?
    var disabled: Boolean?
    var ellipsis: dynamic
    var copyable: dynamic
    var style: dynamic
    var className: String?
    var children: dynamic
}

external val Text: FC<TypographyTextProps>

external interface TypographyTitleProps : Props {
    var level: Number?
    var type: String?
    var disabled: Boolean?
    var ellipsis: dynamic
    var copyable: dynamic
    var style: dynamic
    var className: String?
    var children: dynamic
}

external val Title: FC<TypographyTitleProps>

// ==================== Menu ====================

external interface MenuProps : Props {
    var mode: String?
    var theme: String?
    var selectedKeys: dynamic
    var defaultSelectedKeys: dynamic
    var openKeys: dynamic
    var onSelect: ((dynamic) -> Unit)?
    var onClick: ((dynamic) -> Unit)?
    var items: dynamic
    var style: dynamic
    var className: String?
    var children: dynamic
    var inlineCollapsed: Boolean?
}

external val Menu: FC<MenuProps>

// ==================== Badge ====================

external interface BadgeProps : Props {
    var count: dynamic
    var dot: Boolean?
    var color: String?
    var size: String?
    var overflowCount: Number?
    var showZero: Boolean?
    var status: String?
    var text: dynamic
    var style: dynamic
    var className: String?
    var children: dynamic
    var offset: dynamic
}

external val Badge: FC<BadgeProps>

// ==================== message ====================

external interface MessageInstance {
    fun info(content: String, duration: Number? = definedExternally, onClose: (() -> Unit)? = definedExternally)
    fun success(content: String, duration: Number? = definedExternally, onClose: (() -> Unit)? = definedExternally)
    fun error(content: String, duration: Number? = definedExternally, onClose: (() -> Unit)? = definedExternally)
    fun warning(content: String, duration: Number? = definedExternally, onClose: (() -> Unit)? = definedExternally)
    fun loading(content: String, duration: Number? = definedExternally, onClose: (() -> Unit)? = definedExternally)
    fun destroy()
}

external val message: MessageInstance

// ==================== notification ====================

external interface NotificationInstance {
    fun info(args: dynamic)
    fun success(args: dynamic)
    fun error(args: dynamic)
    fun warning(args: dynamic)
    fun open(args: dynamic)
    fun destroy(key: String? = definedExternally)
}

external val notification: NotificationInstance