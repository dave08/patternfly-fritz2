package org.patternfly

import dev.fritz2.binding.const
import dev.fritz2.dom.DomMountPoint
import dev.fritz2.dom.TextNode
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Label
import dev.fritz2.dom.html.Span
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.dom.clear

// ------------------------------------------------------ dsl

fun HtmlElements.pfSwitch(content: Switch.() -> Unit = {}): Switch =
    register(Switch(), content)

// ------------------------------------------------------ tag

class Switch internal constructor() : Label(baseClass = "switch".component()) {

    var label: Flow<String>
        get() = with(labelTag.domNode.textContent) {
            if (this == null) emptyFlow() else flowOf(this)
        }
        set(value) {
            toggleTag.domNode.clear()
            DomMountPoint(value.map { TextNode(it) }.distinctUntilChanged(), labelTag.domNode)
        }

    var labelOff: Flow<String>
        get() = with(labelOffTag.domNode.textContent) {
            if (this == null) emptyFlow() else flowOf(this)
        }
        set(value) {
            toggleTag.domNode.clear()
            DomMountPoint(value.map { TextNode(it) }.distinctUntilChanged(), labelOffTag.domNode)
        }

    var disabled: Flow<Boolean>
        get() = input.disabled
        set(value) {
            input.disabled = value
        }

    val input: Input
    private val toggleTag: Span
    private val labelTag: Span
    private val labelOffTag: Span

    init {
        val id = Id.unique("switch")
        val onId = Id.unique("switch-on")
        val offId = Id.unique("switch-off")
        domNode.htmlFor = id
        domNode.componentType(ComponentType.Switch)
        input = input(id = id, baseClass = "switch".component("input")) {
            type = const("checkbox")
            aria["labelledby"] = onId
        }
        toggleTag = span(baseClass = "switch".component("toggle")) {
            span(baseClass = "switch".component("toggle", "icon")) {
                pfIcon("check".fas())
            }
        }
        labelTag = span(id = onId, baseClass = "switch".component("label")) {
            domNode.classList += "on".modifier()
            aria["hidden"] = true
        }
        labelOffTag = span(id = offId, baseClass = "switch".component("label")) {
            domNode.classList += "off".modifier()
            aria["hidden"] = true
        }
    }
}