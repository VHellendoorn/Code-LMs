<style class="text/css">
    .Endpoint {
        padding: 10px;
        background: #F1F1F1;
        font-size: 16px;
        font-family: "Courier New", Courier, monospace;
    }
</style>

<div class="Help Aside">
    <h2><?php echo t("Need More Help?"); ?></h2>
    <ul>
        <?php
        echo wrap(
            anchor(
                t("API.Settings.Documentation"),
                "https://github.com/kasperisager/vanilla-api/wiki"
            ),
            "li"
        );
        ?>
    </ul>
</div>

<h1><?php echo t($this->data("Title")); ?></h1>

<?php
$form = $this->Form;
echo $form->open();
echo $form->errors();
?>

<ul>
    <li>
        <?php echo $form->label(t("API.Settings.Endpoint.Label"), "Endpoint"); ?>
        <div class="Info">
            <p><?php echo t("API.Settings.Endpoint.Description"); ?></p>
        </div>
        <div class="Endpoint">
            <blockquote><?php echo Gdn::request()->url("api", true); ?></blockquote>
        </div>
    </li>
    <li>
        <?php echo $form->label(t("API.Settings.Secret.Label"), "Secret"); ?>
        <div class="Info">
            <p><?php echo t("API.Settings.Secret.Description"); ?></p>
            <small>
                <?php
                echo sprintf(
                    t("API.Settings.Refresh.Description"),
                    anchor(
                        t("API.Settings.Refresh.Link"),
                        "http://en.wikipedia.org/wiki/Uuid"
                    )
                );
                ?>
            </small>
        </div>
        <?php
        echo $form->textBox("Secret", ["class" => "InputBox BigInput", "readonly" => "readonly"]);
        echo $form->button(t("API.Settings.Refresh.Label"));
        ?>
    </li>
</ul>

<?php echo $form->close(); ?>
