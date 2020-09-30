package org.patternfly

import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.states
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfToolbar(
    id: String? = null,
    baseClass: String? = null,
    content: Toolbar.() -> Unit = {}
): Toolbar = register(Toolbar(id = id, baseClass = baseClass), content)

fun Toolbar.pfToolbarContent(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarContent.() -> Unit = {}
): ToolbarContent = register(ToolbarContent(id = id, baseClass = baseClass), content)

fun ToolbarContent.pfToolbarContentSection(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarContentSection.() -> Unit = {}
): ToolbarContentSection = register(ToolbarContentSection(id = id, baseClass = baseClass), content)

fun ToolbarContentSection.pfToolbarGroup(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarGroup.() -> Unit = {}
): ToolbarGroup = register(ToolbarGroup(id = id, baseClass = baseClass), content)

fun ToolbarContentSection.pfToolbarItem(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarItem.() -> Unit = {}
): ToolbarItem = register(ToolbarItem(id = id, baseClass = baseClass), content)

fun ToolbarGroup.pfToolbarItem(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarItem.() -> Unit = {}
): ToolbarItem = register(ToolbarItem(id = id, baseClass = baseClass), content)

fun ToolbarContent.pfToolbarExpandableContent(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarExpandableContent.() -> Unit = {}
): ToolbarExpandableContent = register(ToolbarExpandableContent(id = id, baseClass = baseClass), content)

fun ToolbarExpandableContent.pfToolbarGroup(
    id: String? = null,
    baseClass: String? = null,
    content: ToolbarGroup.() -> Unit = {}
): ToolbarGroup = register(ToolbarGroup(id = id, baseClass = baseClass), content)

fun <T> ToolbarItem.pfBulkSelect(
    itemStore: ItemStore<T>,
    id: String? = null,
    baseClass: String? = null,
    content: BulkSelect<T>.() -> Unit = {}
): BulkSelect<T> {
    this.domNode.classList += "bulk-select".modifier()
    return register(BulkSelect(itemStore, id = id, baseClass = baseClass), content)
}

fun <T> ToolbarItem.pfSortOptions(
    itemStore: ItemStore<T>,
    options: Map<String, Comparator<T>>,
    id: String? = null,
    baseClass: String? = null,
    content: SortOptions<T>.() -> Unit = {}
): SortOptions<T> = register(SortOptions(itemStore, options, id = id, baseClass = baseClass), content)

fun <T> ToolbarItem.pfPagination(
    itemStore: ItemStore<T>,
    pageSizes: Array<Int> = PageInfo.DEFAULT_PAGE_SIZES,
    compact: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Pagination.() -> Unit = {}
): Pagination {
    this.domNode.classList += "pagination".modifier()
    return register(
        Pagination(
            itemStore,
            itemStore.data.map { it.pageInfo },
            pageSizes,
            compact,
            id = id,
            baseClass = baseClass
        ), content
    )
}

// ------------------------------------------------------ tag

class Toolbar internal constructor(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Toolbar, baseClass)) {
    init {
        markAs(ComponentType.Toolbar)
    }
}

class ToolbarContent internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("toolbar".component("content"), baseClass))

class ToolbarContentSection internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("toolbar".component("content", "section"), baseClass))

class ToolbarGroup internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("toolbar".component("group"), baseClass))

class ToolbarItem internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("toolbar".component("item"), baseClass))

class ToolbarExpandableContent internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("toolbar".component("expandable", "content"), baseClass))

enum class PreSelection(val text: String) {
    NONE("Select none"), VISIBLE("Select visible"), ALL("Select all")
}

class BulkSelect<T>(itemStore: ItemStore<T>, id: String?, baseClass: String?) :
    Dropdown<PreSelection>(DropdownStore(), dropdownAlign = null, up = false, id = id, baseClass = baseClass) {

    init {
        pfDropdownToggleCheckbox {
            content = {
                itemStore.selected.map {
                    if (it == 0) "" else "$it selected"
                }.bind()
            }
            triState = itemStore.data.map {
                when {
                    it.selected.isEmpty() -> TriState.OFF
                    it.selected.size == it.items.size -> TriState.ON
                    else -> TriState.INDETERMINATE
                }
            }
            input.changes.states().filter { !it }.map { Unit } handledBy itemStore.selectNone
            input.changes.states().filter { it }.map { Unit } handledBy itemStore.selectAll
        }
        display = {
            { +it.item.text }
        }
        store.clicked.unwrap().filter { it == PreSelection.NONE }.map { Unit } handledBy itemStore.selectNone
        store.clicked.unwrap().filter { it == PreSelection.VISIBLE }.map { Unit } handledBy itemStore.selectVisible
        store.clicked.unwrap().filter { it == PreSelection.ALL }.map { Unit } handledBy itemStore.selectAll

        pfDropdownItems {
            PreSelection.values().map { pfItem(it) }
        }
    }
}

sealed class SortOption(val text: String)
class SortProperty<T>(text: String, val comparator: Comparator<T>) : SortOption(text)
class SortOrder(val ascending: Boolean) : SortOption(if (ascending) "Ascending" else "Descending")

class SortOptions<T>(itemStore: ItemStore<T>, options: Map<String, Comparator<*>>, id: String?, baseClass: String?) :
    OptionsMenu<SortOption>(OptionStore(), optionsMenuAlign = null, up = false, id = id, baseClass = baseClass) {

    init {
        display = {
            { +it.item.text }
        }
        pfOptionsMenuToggle { icon = { pfIcon("sort-amount-down".fas()) } }
        pfOptionsMenuGroups {
            pfGroup {
                options.map { (name, comparator) ->
                    pfItem(SortProperty(name, comparator))
                }
            }
            pfSeparator()
            pfGroup {
                pfItem(SortOrder(true)) { selected = true }
                pfItem(SortOrder(false))
            }
        }
        store.selection.unwrap()
            .map { items ->
                val property = items.filterIsInstance<SortProperty<T>>().firstOrNull()
                val order = items.filterIsInstance<SortOrder>().first()
                if (order.ascending) property?.comparator else property?.comparator?.reversed()
            }.filterNotNull() handledBy itemStore.sortWith
    }
}