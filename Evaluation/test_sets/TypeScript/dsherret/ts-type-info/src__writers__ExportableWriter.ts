import CodeBlockWriter from "code-block-writer";
import {ExportableDefinition, InterfaceDefinition, TypeAliasDefinition, AmbientableDefinition} from "./../definitions";
import {WriteFlags} from "./../WriteFlags";

export class ExportableWriter {
    constructor(private readonly writer: CodeBlockWriter) {
    }

    writeExportKeyword(def: ExportableDefinition, flags: WriteFlags) {
        if (!def.isNamedExportOfFile && (def.isDefaultExportOfFile || !def.isExported))
            return;

        let shouldWrite = false;

        if (def instanceof InterfaceDefinition || def instanceof TypeAliasDefinition)
            shouldWrite = (flags & WriteFlags.IsInAmbientContext) === 0;
        else
            shouldWrite = !(def as any as AmbientableDefinition).isAmbient;

        this.writer.conditionalWrite(shouldWrite, "export ");
    }
}
