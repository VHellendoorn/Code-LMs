import * as assert from "@eeue56/ts-assert";
import { Ok } from "@eeue56/ts-core/build/main/lib/result";
import { blockKind, intoBlocks } from "../blocks";
import { compileTypescript } from "../compile";
import { generateJavascript } from "../js_generator";
import { parse } from "../parser";
import { generateTypescript } from "../ts_generator";
import {
    Addition,
    Branch,
    CaseStatement,
    Destructure,
    FixedType,
    FormatStringValue,
    Function,
    FunctionArg,
    Module,
    StringValue,
    UnparsedBlock,
    Value,
} from "../types";

const multiLine = `
sayHiToPet : Animal -> string
sayHiToPet pet =
    case pet of
        Dog { name } -> \`Good boy \${name}!\`
        Cat { lives } -> "You have " + lives + " lives remaining."
`.trim();

const expectedOutput = `
function sayHiToPet(pet: Animal): string {
    const _res = pet;
    switch (_res.kind) {
        case "Dog": {
            const { name } = _res;
            return \`Good boy \${name}!\`;
        }
        case "Cat": {
            const { lives } = _res;
            return "You have " + lives + " lives remaining.";
        }
    }
}
`.trim();

const expectedOutputJS = `
function sayHiToPet(pet) {
    const _res = pet;
    switch (_res.kind) {
        case "Dog": {
            const { name } = _res;
            return \`Good boy \${name}!\`;
        }
        case "Cat": {
            const { lives } = _res;
            return "You have " + lives + " lives remaining.";
        }
    }
}
`.trim();

export function testIntoBlocksMultiLine() {
    assert.deepStrictEqual(intoBlocks(multiLine), [
        UnparsedBlock("FunctionBlock", 0, multiLine.split("\n")),
    ]);
}

export function testBlockKindMultiLine() {
    assert.deepStrictEqual(blockKind(multiLine), Ok("Function"));
}

export function testParseMultiLine() {
    assert.deepStrictEqual(
        parse(multiLine),
        Module(
            "main",
            [
                Function(
                    "sayHiToPet",
                    FixedType("string", [ ]),
                    [ FunctionArg("pet", FixedType("Animal", [ ])) ],
                    [ ],
                    CaseStatement(Value("pet"), [
                        Branch(
                            Destructure("Dog", "{ name }"),
                            FormatStringValue("Good boy ${name}!"),
                            [ ]
                        ),
                        Branch(
                            Destructure("Cat", "{ lives }"),
                            Addition(
                                StringValue("You have "),
                                Addition(
                                    Value("lives"),
                                    StringValue(" lives remaining.")
                                )
                            ),
                            [ ]
                        ),
                    ])
                ),
            ],
            [ ]
        )
    );
}

export function testGenerate() {
    const parsed = parse(multiLine);
    const generated = generateTypescript(parsed);
    assert.strictEqual(generated, expectedOutput);
}

export function testCompileMultiLine() {
    const parsed = parse(multiLine);
    const generated = generateTypescript(parsed);
    const compiled = compileTypescript(generated);

    assert.deepStrictEqual(
        compiled.kind,
        "ok",
        (compiled.kind === "err" && compiled.error.toString()) || ""
    );
}

export function testGenerateJS() {
    const parsed = parse(multiLine);
    const generated = generateJavascript(parsed);
    assert.strictEqual(generated, expectedOutputJS);
}
