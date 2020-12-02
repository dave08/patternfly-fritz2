package org.patternfly

import dev.fritz2.dom.Listener
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import dev.fritz2.elemento.By
import dev.fritz2.elemento.Id
import dev.fritz2.elemento.aria
import dev.fritz2.elemento.querySelector
import dev.fritz2.elemento.removeFromParent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

// TODO Implement overflow and draggable chips
// ------------------------------------------------------ dsl

/**
 * Creates a [Chip] component.
 *
 * @param readOnly whether the chip is read-only. Read-only chips are not closeable.
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.chip(
    readOnly: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Chip.() -> Unit = {}
): Chip = register(Chip(readOnly, id = id, baseClass = baseClass, job), content)

/**
 * Adds a [Badge] component to a [Chip] component.
 *
 * @param min the minimum number to show in the badge
 * @param max the maximum number to show in the badge
 * @param id the ID of the badge component
 * @param baseClass optional CSS class that should be applied to the badge component
 * @param content a lambda expression for setting up the badge component
 */
public fun Chip.badge(
    min: Int = 0,
    max: Int = 999,
    id: String? = null,
    baseClass: String? = null,
    content: Badge.() -> Unit = {}
) {
    domNode.querySelector(By.classname("chip".component("text")))?.let {
        it.parentElement?.insertBefore(
            register(Badge(min, max, id = id, baseClass = baseClass, job), content).domNode, it.nextElementSibling
        )
    }
}

// ------------------------------------------------------ tag

/**
 * PatternFly [chip](https://www.patternfly.org/v4/components/chip/design-guidelines) component.
 *
 * A chip is used to communicate a value or a set of attribute-value pairs within workflows that involve filtering a set of objects.
 *
 * If the chip is not created as read-only, the chip is closeable.
 *
 * @sample ChipSamples.chip
 */
@OptIn(ExperimentalCoroutinesApi::class)
public class Chip internal constructor(readOnly: Boolean, id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLDivElement>,
    WithTextDelegate<HTMLDivElement, HTMLSpanElement>,
    Div(id = id, baseClass = classes {
        +ComponentType.Chip
        +("read-only".modifier() `when` readOnly)
        +baseClass
    }, job) {

    private var textElement: Span
    private var closeButton: PushButton? = null

    /**
     * Listener for the close button (if any).
     *
     * @sample ChipSamples.closes
     */
    public val closes: Listener<MouseEvent, HTMLButtonElement> by lazy { subscribe(closeButton, Events.click) }

    init {
        markAs(ComponentType.Chip)
        val textId = Id.unique(ComponentType.Chip.id, "txt")
        textElement = span(id = textId, baseClass = "chip".component("text")) {}
        if (!readOnly) {
            closeButton = pushButton(ButtonVariation.plain) {
                icon("times".fas())
                aria["label"] = "Remove"
                aria["labelledby"] = textId
                domNode.addEventListener(Events.click.name, this@Chip::close)
            }
        }
    }

    override fun delegate(): HTMLSpanElement = textElement.domNode

    private fun close(@Suppress("UNUSED_PARAMETER") ignore: Event) {
        closeButton?.domNode?.removeEventListener(Events.click.name, ::close)
        domNode.removeFromParent()
    }
}
