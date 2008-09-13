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

package org.metawidget.gwt.client.ui.layout;

import java.util.Map;

import org.metawidget.gwt.client.ui.GwtMetawidget;

import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for all GWT-based layouts.
 * <p>
 * Implementations need not be Thread-safe.
 *
 * @author Richard Kennard
 */

public abstract class Layout
{
	//
	// Private members
	//

	private GwtMetawidget				mMetawidget;

	//
	// Constructor
	//

	public Layout( GwtMetawidget metawidget )
	{
		mMetawidget = metawidget;
	}

	//
	// Public methods
	//

	public void layoutBegin()
	{
		// Do nothing by default
	}

	public void layoutChild( Widget widget, Map<String, String> attributes )
	{
		// Do nothing by default
	}

	public void layoutEnd()
	{
		// Do nothing by default
	}

	//
	// Protected methods
	//

	protected GwtMetawidget getMetawidget()
	{
		return mMetawidget;
	}
}
