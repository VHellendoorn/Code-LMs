import CodeBlockWriter from "code-block-writer";
import {ReExportDefinition} from "./../definitions";
import {BaseDefinitionWriter} from "./BaseDefinitionWriter";
import {NamedImportPartsWriter} from "./NamedImportPartsWriter";

export class ReExportWriter {
    constructor(
        private readonly writer: CodeBlockWriter,
        private readonly baseDefinitionWriter: BaseDefinitionWriter,
        private readonly namedImportPartsWriter: NamedImportPartsWriter
    ) {
    }

    write(def: ReExportDefinition) {
        this.baseDefinitionWriter.writeWrap(def, () => {
            this.writer.write("export ");

            if ((def.namedExports || []).length > 0)
                this.namedImportPartsWriter.write(def.namedExports);
            else
                this.writer.write("*");

            this.writer.write(" from ");

            this.writer.write(`"${def.moduleSpecifier}";`);
        });
    }
}
