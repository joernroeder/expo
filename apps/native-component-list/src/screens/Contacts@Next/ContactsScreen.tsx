import { optionalRequire } from '../../navigation/routeBuilder';
import ComponentListScreen, { ListElement } from '../ComponentListScreen';

export const ContactsNextScreens = [
  {
    name: 'Add contact',
    route: 'contacts@next/add-contact',
    getComponent() {
      return optionalRequire(() => require('./AddContactScreen'));
    },
  },
];

export default function ContactsNextScreen() {
  const apis: ListElement[] = ContactsNextScreens.map((screen) => {
    return {
      name: screen.name,
      isAvailable: true,
      route: `/apis/${screen.route}`,
    };
  });

  return <ComponentListScreen apis={apis} sort={false} />;
}
