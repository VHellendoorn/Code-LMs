// MIT © 2017 azu
"use strict";
import * as assert from "assert";
import { TextlintEditorState } from "../TextlintEditorStore";
describe("TextlintEditorState", () => {
    describe("#editingFileName", () => {
        context("current file path is >=100 length", () => {
            it("should return filename", () => {
                const state = new TextlintEditorState(Object.assign({}, new TextlintEditorState(), {
                    canAccessToFile: true,
                    contentFilePath: `${"/a".repeat(50)  }/file.md`
                }));
                assert.ok(state.editingFileName === "file.md");
            });
        });
        context("current file path is <100 length", () => {
            it("should return file path", () => {
                const contentFilePath = "/path/to/file.md";
                const state = new TextlintEditorState(Object.assign({}, new TextlintEditorState(), {
                    canAccessToFile: true,
                    contentFilePath: contentFilePath
                }));
                assert.ok(state.editingFileName === contentFilePath);
            });
        });
    });
});
