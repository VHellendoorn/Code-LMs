import CodeBlockWriter from "code-block-writer";
import {expect} from "chai";
import {TypeDefinition} from "./../../definitions";
import {TypeWriter} from "./../../writers";

describe(nameof(TypeWriter), () => {
    function getObjects() {
        const writer = new CodeBlockWriter();
        const defWriter = new TypeWriter(writer);
        return {writer, defWriter};
    }

    function doTests(methodName: string, expectedPrefix: string) {
        it("should write the type", () => {
            const {writer, defWriter} = getObjects();
            const def = new TypeDefinition();
            def.text = "string";
            (defWriter as any)[methodName](def, "any");
            expect(writer.toString()).to.equal(expectedPrefix + "string");
        });

        it("should write the fallback type when the definition is null", () => {
            const {writer, defWriter} = getObjects();
            (defWriter as any)[methodName](null, "any");
            expect(writer.toString()).to.equal(expectedPrefix + "any");
        });

        it("should write the fallback type when the definition text is null", () => {
            const {writer, defWriter} = getObjects();
            const def = new TypeDefinition();
            def.text = null as any;
            (defWriter as any)[methodName](def, "any");
            expect(writer.toString()).to.equal(expectedPrefix + "any");
        });

        it("should write the fallback type when the definition text is empty", () => {
            const {writer, defWriter} = getObjects();
            const def = new TypeDefinition();
            def.text = "";
            (defWriter as any)[methodName](def, "any");
            expect(writer.toString()).to.equal(expectedPrefix + "any");
        });

        it("should write the fallback type when the definition text is whitespace", () => {
            const {writer, defWriter} = getObjects();
            const def = new TypeDefinition();
            def.text = "  \t  ";
            (defWriter as any)[methodName](def, "any");
            expect(writer.toString()).to.equal(expectedPrefix + "any");
        });
    }

    describe(nameof<TypeWriter>(w => w.writeWithColon), () => {
        doTests(nameof<TypeWriter>(w => w.writeWithColon), ": ");
    });

    describe(nameof<TypeWriter>(w => w.writeWithEqualsSign), () => {
        doTests(nameof<TypeWriter>(w => w.writeWithEqualsSign), " = ");
    });

    describe(nameof<TypeWriter>(w => w.write), () => {
        doTests(nameof<TypeWriter>(w => w.write), "");
    });
});
