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

package org.metawidget.faces.component.html.widgetbuilder.richfaces;

import static org.metawidget.inspector.InspectionResultConstants.*;
import static org.metawidget.inspector.faces.FacesInspectionResultConstants.*;

import java.awt.Color;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.metawidget.faces.component.UIMetawidget;
import org.metawidget.faces.component.html.HtmlMetawidget;
import org.metawidget.util.ClassUtils;
import org.metawidget.widgetbuilder.impl.BaseWidgetBuilder;
import org.richfaces.component.UICalendar;
import org.richfaces.component.UIInputNumberSlider;
import org.richfaces.component.UIInputNumberSpinner;
import org.richfaces.component.html.HtmlInputNumberSpinner;

/**
 * WidgetBuilder for RichFaces environments.
 * <p>
 * Automatically creates native RichFaces UIComponents, such as <code>HtmlCalendar</code> and
 * <code>HtmlInputNumberSlider</code>, to suit the inspected fields.
 *
 * @author Richard Kennard
 */

public class RichFacesWidgetBuilder
	extends BaseWidgetBuilder<UIComponent, UIMetawidget>
{
	//
	// Protected methods
	//

	@Override
	protected UIComponent buildReadOnlyWidget( String elementName, Map<String, String> attributes, UIMetawidget metawidget )
		throws Exception
	{
		// Not for RichFaces?

		if ( TRUE.equals( attributes.get( HIDDEN ) ) )
			return null;

		if ( attributes.containsKey( FACES_LOOKUP ) || attributes.containsKey( LOOKUP ) )
			return null;

		String type = getType( attributes );

		if ( type == null)
			return null;

		Class<?> clazz = ClassUtils.niceForName( type );

		if ( clazz == null )
			return null;

		// Color

		if ( Color.class.isAssignableFrom( clazz ))
			return createReadOnlyComponent( attributes, metawidget );

		// Not for RichFaces

		return null;
	}

	@Override
	protected UIComponent buildActiveWidget( String elementName, Map<String, String> attributes, UIMetawidget metawidget )
		throws Exception
	{
		// Not for RichFaces?

		if ( TRUE.equals( attributes.get( HIDDEN ) ) )
			return null;

		if ( attributes.containsKey( FACES_LOOKUP ) || attributes.containsKey( LOOKUP ) )
			return null;

		Application application = FacesContext.getCurrentInstance().getApplication();
		String type = getType( attributes );

		if ( type == null)
			return null;

		Class<?> clazz = ClassUtils.niceForName( type );

		if ( clazz == null )
			return null;

		// Primitives

		if ( clazz.isPrimitive() )
		{
			// Not for RichFaces

			if ( boolean.class.equals( clazz ) || char.class.equals( clazz ) )
				return null;

			// Ranged

			String minimumValue = attributes.get( MINIMUM_VALUE );
			String maximumValue = attributes.get( MAXIMUM_VALUE );

			if ( minimumValue != null && !"".equals( minimumValue ) && maximumValue != null && !"".equals( maximumValue ) )
			{
				UIInputNumberSlider slider = (UIInputNumberSlider) application.createComponent( "org.richfaces.inputNumberSlider" );
				slider.setMinValue( minimumValue );
				slider.setMaxValue( maximumValue );

				return slider;
			}

			// Not-ranged

			UIInputNumberSpinner spinner = (UIInputNumberSpinner) application.createComponent( "org.richfaces.inputNumberSpinner" );

			// May be ranged in one dimension only

			if ( minimumValue != null && !"".equals( minimumValue ) )
				spinner.setMinValue( minimumValue );
			else if ( byte.class.equals( clazz ) )
				spinner.setMinValue( String.valueOf( Byte.MIN_VALUE ) );
			else if ( short.class.equals( clazz ) )
				spinner.setMinValue( String.valueOf( Short.MIN_VALUE ) );
			else if ( int.class.equals( clazz ) )
				spinner.setMinValue( String.valueOf( Integer.MIN_VALUE ) );
			else if ( long.class.equals( clazz ) )
				spinner.setMinValue( String.valueOf( Long.MIN_VALUE ) );
			else if ( float.class.equals( clazz ) )
				spinner.setMinValue( String.valueOf( -Float.MAX_VALUE ) );
			else if ( double.class.equals( clazz ) )
				spinner.setMinValue( String.valueOf( -Double.MAX_VALUE ) );

			if ( maximumValue != null && !"".equals( maximumValue ) )
				spinner.setMaxValue( maximumValue );
			else if ( byte.class.equals( clazz ) )
				spinner.setMaxValue( String.valueOf( Byte.MAX_VALUE ) );
			else if ( short.class.equals( clazz ) )
				spinner.setMaxValue( String.valueOf( Short.MAX_VALUE ) );
			else if ( int.class.equals( clazz ) )
				spinner.setMaxValue( String.valueOf( Integer.MAX_VALUE ) );
			else if ( long.class.equals( clazz ) )
				spinner.setMaxValue( String.valueOf( Long.MAX_VALUE ) );
			else if ( float.class.equals( clazz ) )
				spinner.setMaxValue( String.valueOf( Float.MAX_VALUE ) );
			else if ( double.class.equals( clazz ) )
				spinner.setMaxValue( String.valueOf( Double.MAX_VALUE ) );

			// HtmlInputNumberSpinner-specific properties

			if ( spinner instanceof HtmlInputNumberSpinner )
			{
				HtmlInputNumberSpinner htmlSpinner = (HtmlInputNumberSpinner) spinner;

				// Wraps around?

				htmlSpinner.setCycled( false );

				// Stepped

				if ( float.class.equals( clazz ) || double.class.equals( clazz ) )
					htmlSpinner.setStep( "0.1" );
			}

			return spinner;
		}

		// Dates
		//
		// Note: when http://jira.jboss.org/jira/browse/RF-2023 gets implemented, that
		// would allow external, app-level configuration of this Calendar

		if ( Date.class.isAssignableFrom( clazz ) )
		{
			UICalendar calendar = (UICalendar) application.createComponent( "org.richfaces.Calendar" );

			if ( attributes.containsKey( DATETIME_PATTERN ) )
				calendar.setDatePattern( attributes.get( DATETIME_PATTERN ) );

			if ( attributes.containsKey( LOCALE ) )
				calendar.setLocale( new Locale( attributes.get( LOCALE ) ) );

			if ( attributes.containsKey( TIME_ZONE ) )
				calendar.setTimeZone( TimeZone.getTimeZone( attributes.get( TIME_ZONE ) ) );

			return calendar;
		}

		// Object primitives

		if ( Number.class.isAssignableFrom( clazz ) )
		{
			// Ranged

			String minimumValue = attributes.get( MINIMUM_VALUE );
			String maximumValue = attributes.get( MAXIMUM_VALUE );

			if ( minimumValue != null && !"".equals( minimumValue ) && maximumValue != null && !"".equals( maximumValue ) )
			{
				UIInputNumberSlider slider = (UIInputNumberSlider) application.createComponent( "org.richfaces.inputNumberSlider" );
				slider.setMinValue( minimumValue );
				slider.setMaxValue( maximumValue );

				return slider;
			}

			// Not-ranged
			//
			// Until https://jira.jboss.org/jira/browse/RF-4450 is fixed, do not use
			// UIInputNumberSpinner for nullable numbers
		}

		// Colors (as of RichFaces 3.3.1)

		if ( Color.class.isAssignableFrom( clazz ))
			return application.createComponent( "org.richfaces.ColorPicker" );

		// Not for RichFaces

		return null;
	}

	//
	// Private methods
	//

	private UIComponent createReadOnlyComponent( Map<String, String> attributes, UIMetawidget metawidget )
	{
		Application application = FacesContext.getCurrentInstance().getApplication();

		// Note: it is important to use 'javax.faces.HtmlOutputText', not just 'javax.faces.Output',
		// because the latter is not HTML escaped (according to the JSF 1.2 spec)

		UIComponent readOnlyComponent = application.createComponent( "javax.faces.HtmlOutputText" );

		if ( !( (HtmlMetawidget) metawidget ).isCreateHiddenFields() || TRUE.equals( attributes.get( NO_SETTER ) ) )
			return readOnlyComponent;

		// If using hidden fields, create both a label and a hidden field

		UIComponent componentStub = application.createComponent( "org.metawidget.Stub" );

		List<UIComponent> children = componentStub.getChildren();

		children.add( application.createComponent( "javax.faces.HtmlInputHidden" ) );
		children.add( readOnlyComponent );

		return componentStub;
	}
}