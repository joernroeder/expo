"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.StackHeaderItem = StackHeaderItem;
exports.convertStackHeaderItemPropsToRNHeaderItem = convertStackHeaderItemPropsToRNHeaderItem;
function StackHeaderItem(props) {
    return null;
}
function convertStackHeaderItemPropsToRNHeaderItem(props) {
    if (!props.children) {
        console.warn('Stack.Header.Item requires a child element to render custom content in the header.');
    }
    return {
        ...props,
        type: 'custom',
        element: props.children ?? <></>,
    };
}
//# sourceMappingURL=StackHeaderItem.js.map