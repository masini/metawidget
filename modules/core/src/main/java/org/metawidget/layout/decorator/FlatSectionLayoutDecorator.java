// Metawidget
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package org.metawidget.layout.decorator;

import java.util.Arrays;
import java.util.Map;

/**
 * Convenience base class for LayoutDecorators wishing to decorate widgets based on changing
 * sections, rendering multi-level sections (ie. section="foo,bar") as siblings.
 * <p>
 * Because sections are rendered as siblings, <code>FlatSectionLayoutDecorator</code> never creates
 * a sub <code>container</code>.
 *
 * @author Richard Kennard
 */

public abstract class FlatSectionLayoutDecorator<W, C extends W, M extends C>
	extends LayoutDecorator<W, C, M> {

	//
	// Constructor
	//

	protected FlatSectionLayoutDecorator( LayoutDecoratorConfig<W, C, M> config ) {

		super( config );
	}

	//
	// Public methods
	//

	@Override
	public void startContainerLayout( C container, M metawidget ) {

		super.startContainerLayout( container, metawidget );

		State state = getState( container, metawidget );
		state.currentSections = null;
	}

	@Override
	public void layoutWidget( W widget, String elementName, Map<String, String> attributes, C container, M metawidget ) {

		// If our delegate is itself a NestedSectionLayoutDecorator, strip the section

		State state = getState( container, metawidget );

		if ( getDelegate() instanceof NestedSectionLayoutDecorator<?, ?, ?> ) {

			String section = stripSection( attributes );

			// Stay where we are?
			//
			// Note: Ignore empty stubs. Do not create a new section in case it ends up being empty

			if ( isIgnored( widget ) || section == null || ( state.currentSections != null && section.equals( state.currentSections[0] ) ) ) {
				super.layoutWidget( widget, elementName, attributes, container, metawidget );
				return;
			}

			// End nested LayoutDecorator's current section

			if ( state.currentSections != null && !section.equals( state.currentSections[0] ) ) {
				super.endContainerLayout( container, metawidget );
			}

			state.currentSections = new String[] { section };

			// Add a heading

			if ( !"".equals( section ) ) {
				addSectionWidget( section, 0, container, metawidget );
			}
		} else {

			String[] sections = getSections( attributes );

			// Stay where we are?
			//
			// Note: Ignore empty stubs. Do not create a new section in case it ends up being empty

			if ( isIgnored( widget ) || sections.length == 0 || Arrays.equals( sections, state.currentSections ) ) {
				super.layoutWidget( widget, elementName, attributes, container, metawidget );
				return;
			}

			// For each of the new sections...

			for ( int level = 0; level < sections.length; level++ ) {
				String section = sections[level];

				// ...that are different from our current...

				if ( "".equals( section ) ) {
					continue;
				}

				if ( state.currentSections != null && level < state.currentSections.length && section.equals( state.currentSections[level] ) ) {
					continue;
				}

				// ...add a heading
				//
				// Note: we cannot stop/start the delegate layout here. It is tempting, but remember
				// addSectionWidget needs to use the delegate. If you stop/add section heading/start
				// the delegate, who is laying out the section heading?

				addSectionWidget( section, level, container, metawidget );
			}

			state.currentSections = sections;
		}

		// Add component as normal

		super.layoutWidget( widget, elementName, attributes, container, metawidget );
	}

	//
	// Protected methods
	//

	protected abstract String stripSection( Map<String, String> attributes );

	protected abstract String[] getSections( Map<String, String> attributes );

	protected abstract State getState( C container, M metawidget );

	/**
	 * Returns <code>true</code> if the given widget should be ignored, and no section heading
	 * created for it. For example, the widget might be an empty Stub widget.
	 * <p>
	 * Subclasses should override this method and return <code>true</code> if the widget is some
	 * other kind of ignored component (eg. <code>HtmlInputHidden</code>).
	 */

	protected abstract boolean isIgnored( W widget );

	/**
	 * @param section
	 *            section text (needs localizing)
	 * @param level
	 *            level of section heading (ie. 0=highest, 1=next level down etc.)
	 * @param container
	 * @param metawidget
	 */

	protected abstract void addSectionWidget( String section, int level, C container, M metawidget );

	//
	// Inner class
	//

	/**
	 * Simple, lightweight structure for saving state.
	 */

	public static class State {

		public String[]	currentSections;
	}
}
