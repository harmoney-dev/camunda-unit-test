package pme123.camunda.dmn.tester.client.runner

import boopickle.Default._
import org.scalablytyped.runtime.StringDictionary
import pme123.camunda.dmn.tester.client._
import pme123.camunda.dmn.tester.client.services.AjaxClient
import pme123.camunda.dmn.tester.shared.{DmnApi, DmnConfig, TesterInput}
import slinky.core.FunctionalComponent
import slinky.core.WithAttrs.build
import slinky.core.annotations.react
import slinky.core.facade.Fragment
import slinky.core.facade.Hooks.useEffect
import slinky.web.html.p
import typings.antDesignIconsSvg.mod.{MinusCircleOutlined, PlusOutlined}
import typings.antd.components._
import typings.antd.formFormMod.useForm
import typings.antd.formListMod.{FormListFieldData, FormListOperation}
import typings.antd.mod.message
import typings.rcFieldForm.interfaceMod.{BaseRule, Store}
import typings.react.mod.CSSProperties

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.{JSON, RegExp}
import scala.util.{Failure, Success}

@react object DmnConfigForm {

  case class Props(
      basePath: String,
      maybeDmnConfig: Option[DmnConfig],
      isModalVisible: Boolean,
      onSave: Store => Unit,
      onCancel: () => Unit
  )

  val component: FunctionalComponent[Props] = FunctionalComponent[Props] {
    props =>
      val Props(basePath, maybeDmnConfig, isModalVisible, onSave, onCancel) =
        props
      val form = useForm().head

      useEffect(
        () => {
          if (isModalVisible)
            form.setFieldsValue(
              StringDictionary(
                "decisionId" -> s"${maybeDmnConfig.map(_.decisionId).getOrElse("")}",
                "pathOfDmn" -> s"${maybeDmnConfig.map(_.dmnPath.mkString("/")).getOrElse("")}",
                "testerInputs" -> js.Array(
                  maybeDmnConfig.toSeq.flatMap(_.data.inputs).map {
                    case ti @ TesterInput(key, values) =>
                      StringDictionary(
                        "key" -> key,
                        "type" -> ti.valueType,
                        "values" -> ti.valuesAsString
                      )
                  }: _*
                )
              )
            )
        },
        Seq(isModalVisible)
      )

      val identifierRule = BaseRule()
        .setRequired(true)
        .setPattern(RegExp("""^[^\d\W][-\w]+$""", "gm"))
        .setMessage("This is required and must a valid Identifier!")

      Modal
        .title("DMN Config Editor")
        .okText(textWithTooltip("Save", "BE AWARE, that this will delete all TestCases of an existing DmnConfig."))
        .width(1000)
        .visible(isModalVisible)
        .forceRender(true)
        .onOk(_ =>
          form
            .validateFields()
            .toFuture
            .map { (values: Store) =>
              form.resetFields()
              onSave(values)
              values
            }
        )
        .onCancel(_ => onCancel())(
          Form
            .form(form)
            .className("config-form")(
              FormItem
                .withKey("decisionIdKey")
                .name("decisionId")
                .label(
                  textWithTooltip(
                    "Decision Id",
                    "The Id of the DMN Table you want to test. Valid examples: my-dmn, my_dmn3"
                  )
                )
                .rulesVarargs(identifierRule)(
                  Input()
                ),
              FormItem
                .name("pathOfDmn") // dmnPath did not work!?
                .withKey("pathOfDmnKey")
                .label(
                  textWithTooltip(
                    s"DMN Path $basePath",
                    s"The relative Path of the DMN to the Base Path. Example: core/src/test/resources/numbers.dmn"
                  )
                )
                .rulesVarargs(
                  BaseRule()
                    .setRequired(true)
                    .setPattern(RegExp("""^[^\/](.+\/)*.+\.dmn$""", "gm"))
                    .setMessage(
                      "The DMN Path is required in the following format: path/to/dmn/my.dmn!"
                    )
                )(
                  Input()
                ),
              p("Test Inputs"),
              FormList(
                children = (
                    fields: js.Array[FormListFieldData],
                    op: FormListOperation
                ) => {
                  Fragment.withKey("testInputsKey")(
                    fields.map { field =>
                      Row
                        .withKey(field.key.toString)(
                          Col
                            .style(CSSProperties().setPaddingRight(10))
                            .span(5)(
                              FormItem
                                .withKey(field.fieldKey + "key")
                                .label(
                                  textWithTooltip(
                                    "Key",
                                    "The input variable key needed in the DMN Table."
                                  )
                                )
                                .nameVarargs(field.name, "key")
                                .fieldKeyVarargs(field.fieldKey + "key")
                                .rulesVarargs(identifierRule)(
                                  Input()
                                )
                            ),
                          Col
                            .style(CSSProperties().setPaddingRight(10))
                            .span(5)(
                              FormItem
                                .withKey(field.fieldKey + "type")
                                .label(
                                  textWithTooltip(
                                    "Type",
                                    "The type of your inputs."
                                  )
                                )
                                .initialValue("String")
                                .nameVarargs(field.name, "type")
                                .fieldKeyVarargs(field.fieldKey + "type")(
                                  Select[String].apply(
                                    Select.Option("String")("String"),
                                    Select.Option("Number")("Number"),
                                    Select.Option("Boolean")("Boolean")
                                  )
                                )
                            ),
                          Col
                            .style(CSSProperties().setPaddingRight(10))
                            .span(13)(
                              FormItem
                                .withKey(field.fieldKey + "values")
                                .label(
                                  textWithTooltip(
                                    "Values",
                                    "All inputs for this input you want to test. Example: ch,fr,de,CH"
                                  )
                                )
                                .fieldKeyVarargs(field.fieldKey + "values")
                                .nameVarargs(field.name, "values")
                                .rulesVarargs(
                                  BaseRule()
                                    .setRequired(true)
                                    .setPattern(
                                      RegExp("""^[^,]+(,[^,]+)*$""", "gm")
                                    )
                                    .setMessage(
                                      "The Test Input Values is required!"
                                    )
                                )(
                                  Input()
                                )
                            ),
                          Col.span(1)(
                            iconWithTooltip(
                              MinusCircleOutlined,
                              "Delete this Test Input.",
                              () => op.remove(field.name)
                            )
                          )
                        )
                    }.toSeq :+
                      FormItem
                        .withKey("addInput")
                        .name("addInput")(
                          buttonWithTextTooltip(
                            PlusOutlined,
                            "Add Input",
                            "Add an Input of the DMN Table.",
                            () => op.add()
                          )
                        )
                  )
                },
                name = "testerInputs"
              )
            )
        )
  }

}
