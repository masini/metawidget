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

package org.metawidget.inspector.impl.propertystyle;

import java.lang.annotation.Annotation;

/**
 * Convenience implementation for Properties.
 * <p>
 * Handles construction, and returning names and types.
 *
 * @author Richard Kennard
 */

public abstract class BaseProperty
	implements Property
{
	//
	// Private methods
	//

	private String		mName;

	private Class<?>	mType;

	//
	// Constructor
	//

	public BaseProperty( String name, Class<?> type )
	{
		mName = name;
		mType = type;
	}

	//
	// Public methods
	//

	public String getName()
	{
		return mName;
	}

	public Class<?> getType()
	{
		return mType;
	}

	public boolean isAnnotationPresent( Class<? extends Annotation> annotation )
	{
		return ( getAnnotation( annotation ) != null );
	}

	@Override
	public String toString()
	{
		return mName;
	}
}
