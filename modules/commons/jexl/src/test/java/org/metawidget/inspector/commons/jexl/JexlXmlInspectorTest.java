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

package org.metawidget.inspector.commons.jexl;

import static org.metawidget.inspector.InspectionResultConstants.*;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.metawidget.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Richard Kennard
 */

public class JexlXmlInspectorTest
	extends TestCase {

	//
	// Public methods
	//

	public void testXml() {

		String xml = "<?xml version=\"1.0\"?>";
		xml += "<inspection-result xmlns=\"http://www.metawidget.org/inspection-result\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.metawidget.org/inspection-result ../../inspector/inspection-result-1.0.xsd\" version=\"1.0\">";
		xml += "<entity type=\"org.metawidget.inspector.commons.jexl.JexlXmlInspectorTest$Foo\">";
		xml += "<property name=\"bar1\" value-is-el=\"${this.baz}\" value-is-text=\"text\"/>";
		xml += "<action name=\"bar2\" value-is-el=\"${this.baz}\" value-is-text=\"text\"/>";
		xml += "</entity></inspection-result>";

		JexlXmlInspector inspector = new JexlXmlInspector( new JexlXmlInspectorConfig().setInputStream( new ByteArrayInputStream( xml.getBytes() ) ) );

		String result = inspector.inspect( new Foo(), Foo.class.getName() );
		Document document = XmlUtils.documentFromString( result );

		assertEquals( "inspection-result", document.getFirstChild().getNodeName() );

		// Entity

		Element entity = (Element) document.getFirstChild().getFirstChild();
		assertEquals( ENTITY, entity.getNodeName() );
		assertEquals( "org.metawidget.inspector.commons.jexl.JexlXmlInspectorTest$Foo", entity.getAttribute( TYPE ) );
		assertFalse( entity.hasAttribute( NAME ) );

		// Properties

		Element property = XmlUtils.getChildWithAttributeValue( entity, NAME, "bar1" );
		assertEquals( PROPERTY, property.getNodeName() );
		assertEquals( "from-baz", property.getAttribute( "value-is-el" ) );
		assertEquals( "text", property.getAttribute( "value-is-text" ) );
		assertTrue( 3 == property.getAttributes().getLength() );

		// Actions

		Element action = XmlUtils.getChildWithAttributeValue( entity, NAME, "bar2" );
		assertEquals( ACTION, action.getNodeName() );
		assertEquals( "from-baz", action.getAttribute( "value-is-el" ) );
		assertEquals( "text", action.getAttribute( "value-is-text" ) );
		assertTrue( 3 == action.getAttributes().getLength() );

		assertTrue( entity.getChildNodes().getLength() == 2 );

		// Test null

		result = inspector.inspect( null, Foo.class.getName() );
		document = XmlUtils.documentFromString( result );
		entity = (Element) document.getFirstChild().getFirstChild();

		property = XmlUtils.getChildWithAttributeValue( entity, NAME, "bar1" );
		assertEquals( "", property.getAttribute( "value-is-el" ) );
		assertTrue( 3 == property.getAttributes().getLength() );

		action = XmlUtils.getChildWithAttributeValue( entity, NAME, "bar2" );
		assertEquals( "", action.getAttribute( "value-is-el" ) );
		assertTrue( 3 == action.getAttributes().getLength() );

		assertTrue( entity.getChildNodes().getLength() == 2 );
	}

	//
	// Inner class
	//

	public static class Foo {

		public String getBaz() {

			return "from-baz";
		}
	}
}