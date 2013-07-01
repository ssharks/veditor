//mg
package net.sourceforge.veditor.templates;

import org.eclipse.jface.text.templates.SimpleTemplateVariableResolver;
import org.eclipse.jface.text.templates.TemplateContext;

public class VhdlTemplateVariable {
	
	public static class Brief extends SimpleTemplateVariableResolver {
		/**
		 * Creates a new user name variable
		 */
		public Brief() {
			super("brief", "Brief description."); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * {@inheritDoc}
		 */
		protected String resolve(TemplateContext context) {
			return null;
		}
	}

}
//mg----------------------------