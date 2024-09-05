"use client"

import * as React from "react"

import {Button} from "@/components/ui/button"
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
} from "@/components/ui/command"
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover"
import {useEffect} from "react";

export type Status = {
    value: string
    label: string
}

type ComboboxPopoverProps = {
    statuses: Status[]
    defaultStatus?: Status | null
    onStatusChange: (status: Status | null) => void
}

export function ComboboxPopover({statuses, defaultStatus = null, onStatusChange}: ComboboxPopoverProps) {
    const [open, setOpen] = React.useState(false)
    const [selectedStatus, setSelectedStatus] = React.useState<Status | null>(defaultStatus)

    useEffect(() => {
        handleStatusChange(selectedStatus)
    }, [selectedStatus]);

    const handleStatusChange = (status: Status | null) => {
        setSelectedStatus(status)
        onStatusChange(status)
    }

    return (
        <div className="flex items-center space-x-4">
            <p className="text-sm text-muted-foreground">Status</p>
            <Popover open={open} onOpenChange={setOpen}>
                <PopoverTrigger asChild>
                    <Button variant="outline" className="w-[150px] justify-start">
                        {selectedStatus ? <>{selectedStatus.label}</> : <>+ Set status</>}
                    </Button>
                </PopoverTrigger>
                <PopoverContent className="p-0" side="right" align="start">
                    <Command>
                        <CommandInput placeholder="Change status..."/>
                        <CommandList>
                            <CommandEmpty>No results found.</CommandEmpty>
                            <CommandGroup>
                                {statuses.map((status) => (
                                    <CommandItem
                                        key={status.value}
                                        value={status.value}
                                        onSelect={(value) => {
                                            setSelectedStatus(
                                                statuses.find((priority) => priority.value === value) ||
                                                null
                                            )
                                            setOpen(false)
                                        }}
                                    >
                                        {status.label}
                                    </CommandItem>
                                ))}
                            </CommandGroup>
                        </CommandList>
                    </Command>
                </PopoverContent>
            </Popover>
        </div>
    )
}
