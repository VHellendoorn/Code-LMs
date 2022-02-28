import {getInfoFromString} from "./../../../main";
import {runFileDefinitionTests} from "./../../testHelpers";

describe("prefix type tests", () => {
    const code = `
namespace MyNamespace {
    export class MyClass {
    }
    export type MyTypeAlias = { prop: string; };
}

let t: MyNamespace.MyClass;
let u: MyNamespace.MyTypeAlias;
`;

    const def = getInfoFromString(code);

    runFileDefinitionTests(def, {
        namespaces: [{
            name: "MyNamespace",
            classes: [{
                name: "MyClass",
                isExported: true
            }],
            typeAliases: [{
                name: "MyTypeAlias",
                type: { text: "{ prop: string; }" },
                isExported: true
            }],
            exports: [{
                name: "MyClass"
            }, {
                name: "MyTypeAlias"
            }]
        }],
        variables: [{
            name: "t",
            type: {
                text: "MyNamespace.MyClass",
                node: {
                    text: "MyNamespace.MyClass"
                }
            }
        }, {
            name: "u",
            type: {
                text: "MyNamespace.MyTypeAlias",
                node: {
                    text: "MyNamespace.MyTypeAlias"
                }
            }
        }]
    });
});
