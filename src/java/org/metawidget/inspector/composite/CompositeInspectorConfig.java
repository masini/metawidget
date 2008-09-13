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

package org.metawidget.inspector.composite;

import org.metawidget.inspector.iface.Inspector;

/**
 * Configures a CompositeInspector prior to use. Once instantiated, Inspectors are immutable.
 *
 * @author Richard Kennard
 */

public class CompositeInspectorConfig
{
	//
	// Private members
	//

	private Inspector[]	mInspectors;

	private boolean		mValidating;

	//
	// Public methods
	//

	public Inspector[] getInspectors()
	{
		return mInspectors;
	}

	/**
	 * Sets the sub-Inspectors the CompositeInspector will call.
	 * <p>
	 * Inspectors will be called in order. CompositeInspector's merging
	 * algorithm preserves the element ordering of the first DOMs as new DOMs are merged in.
	 */

	public void setInspectors( Inspector... inspectors )
	{
		mInspectors = inspectors;
	}

	public boolean isValidating()
	{
		return mValidating;
	}

	/**
	 * Sets whether the CompositeInspector should validate all DOMs returned by sub-Inspectors.
	 * <p>
	 * DOMs will be validated against <code>inspection-result-1.0.xsd</code>. Useful when
	 * developing new Inspectors, but carries a performance penalty. False by default.
	 *
	 * @param validating
	 */

	public void setValidating( boolean validating )
	{
		mValidating = validating;
	}
}
